package com.ligainternaetsiinf.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.LigaFantasyResponse;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.model.Mercado;
import com.ligainternaetsiinf.model.User;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.JugadorRepository;
import com.ligainternaetsiinf.repository.LigaFantasyRepository;
import com.ligainternaetsiinf.repository.MercadoRepository;
import com.ligainternaetsiinf.repository.UserRepository;

@Service
public class LigaFantasyService {

    @Autowired
    private LigaFantasyRepository ligaFantasyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipoFantasyRepository equipoFantasyRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private JugadorFantasyRepository jugadorFantasyRepository;

    @Autowired
    private MercadoRepository mercadoRepository;

    public LigaFantasyResponse crearLiga(String nombre, Integer userId){

        User creador = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String code = UUID.randomUUID().toString().substring(0,6);

        LigaFantasy liga = new LigaFantasy(nombre, code, creador);

        ligaFantasyRepository.save(liga);        

        // Crear jugadorFantasy para cada jugador real
        List<Jugador> jugadoresReales = jugadorRepository.findAll();
        List<JugadorFantasy> jugadoresFantasy = new ArrayList<>();

        for(Jugador j : jugadoresReales){
            jugadoresFantasy.add(new JugadorFantasy(j, liga));
        }

        jugadorFantasyRepository.saveAll(jugadoresFantasy);
        
        // Crear equipo fantasy del creador
        List<JugadorFantasy> plantilla = asignarEquipoInicial(liga);

        EquipoFantasy equipo = new EquipoFantasy(liga, creador, plantilla);

        equipoFantasyRepository.save(equipo);

        for(JugadorFantasy jf : plantilla){
            jf.setEquipoFantasy(equipo);
        }

        jugadorFantasyRepository.saveAll(plantilla);

        // Crear mercado
        //Mercado mercado = new Mercado(liga, jugadorFantasyRepository.findByLigaFantasyId(liga.getId()));
        Mercado mercado = new Mercado(liga);
        mercadoRepository.save(mercado);

        return cambioTipoRespuesta(liga);
    }

    public LigaFantasyResponse unirseALiga(String code, Integer userId){

        LigaFantasy liga = ligaFantasyRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Liga no encontrada"));

        User usuario = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // comprobar si ya está en la liga
        if(equipoFantasyRepository.existsByLigaFantasyAndUser(liga, usuario)){
            throw new RuntimeException("El usuario ya está en esta liga");
        }

        // comprobar máximo de jugadores
        if(equipoFantasyRepository.countByLigaFantasy(liga) >= liga.getMaxPlayers()){
            throw new RuntimeException("La liga está llena");
        }

        // crear equipo fantasy
        List<JugadorFantasy> plantilla = asignarEquipoInicial(liga);

        EquipoFantasy equipo = new EquipoFantasy(liga, usuario, plantilla);   
        equipoFantasyRepository.save(equipo);    

        for(JugadorFantasy jf : plantilla){
            jf.setEquipoFantasy(equipo);
        }

        jugadorFantasyRepository.saveAll(plantilla);

        return cambioTipoRespuesta(liga);
    }

    public LigaFantasyResponse obtenerLiga(Integer ligaId){

        LigaFantasy liga = ligaFantasyRepository.findById(ligaId)
        .orElseThrow(() -> new RuntimeException("Liga no encontrada"));

        return cambioTipoRespuesta(liga);
    }
    

    public List<LigaFantasyResponse> obtenerLigasDeUsuario(Integer userId){
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<LigaFantasy> ligas = equipoFantasyRepository.findLigasByUserId(userId);

        List<LigaFantasyResponse> resultado = new ArrayList<>();

        for(LigaFantasy liga : ligas){
            resultado.add(cambioTipoRespuesta(liga));
        }

        return resultado;
    }

    public void abandonarLiga(Integer ligaId, Integer userId){

        EquipoFantasy equipo = equipoFantasyRepository
            .findByLigaFantasyIdAndUserId(ligaId, userId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));


        for(JugadorFantasy j : equipo.getJugadores()){
            j.setEquipoFantasy(null);
            jugadorFantasyRepository.save(j);
        }
        equipoFantasyRepository.delete(equipo);
    }




    private List<JugadorFantasy> asignarEquipoInicial(LigaFantasy liga) {
        List<JugadorFantasy> disponibles = jugadorFantasyRepository
            .findByLigaFantasyIdAndEquipoFantasyIsNull(liga.getId());

        List<JugadorFantasy> porteros     = new ArrayList<>();
        List<JugadorFantasy> defensas     = new ArrayList<>();
        List<JugadorFantasy> mediocentros = new ArrayList<>();
        List<JugadorFantasy> delanteros   = new ArrayList<>();

        for (JugadorFantasy jf : disponibles) {
            switch (jf.getJugadorReal().getPosicion()) {
                case "PORTERO"     -> porteros.add(jf);
                case "DEFENSA"     -> defensas.add(jf);
                case "MEDIOCENTRO" -> mediocentros.add(jf);
                case "DELANTERO"   -> delanteros.add(jf);
            }
        }

        Random random = new Random();
        List<JugadorFantasy> plantilla = new ArrayList<>();

        // 1 portero
        plantilla.add(elegirJugadorEquilibrado(porteros, random));
        // 2 defensas
        plantilla.addAll(elegirNJugadores(defensas, 2, random, "defensas"));
        // 3 mediocentros
        plantilla.addAll(elegirNJugadores(mediocentros, 2, random, "mediocentros"));
        // 1 delantero
        plantilla.addAll(elegirNJugadores(delanteros, 2, random, "delanteros"));

        if (plantilla.size() < 7) {
            throw new RuntimeException("No hay suficientes jugadores para crear una plantilla");
        }

        return plantilla;
    }

    private JugadorFantasy elegirJugadorEquilibrado(List<JugadorFantasy> lista, Random random) {
        if (lista.isEmpty()) throw new RuntimeException("No hay jugadores disponibles para esta posición");
        List<JugadorFantasy> normales = lista.stream()
            .filter(j -> j.getJugadorReal().getValorMercado() >= 2000000
                    && j.getJugadorReal().getValorMercado() <= 15000000)
            .collect(java.util.stream.Collectors.toList());
        List<JugadorFantasy> fuente = normales.isEmpty() ? lista : normales;
        return fuente.get(random.nextInt(fuente.size()));
    }

    private List<JugadorFantasy> elegirNJugadores(List<JugadorFantasy> lista, int n,
            Random random, String posicion) {
        
        if (lista.isEmpty()) throw new RuntimeException("No hay suficientes " + posicion + " disponibles");
        
        List<JugadorFantasy> disponibles = lista.stream()
            .filter(j -> j.getJugadorReal().getValorMercado() >= 2000000
                    && j.getJugadorReal().getValorMercado() <= 15000000)
            .collect(java.util.stream.Collectors.toList());

        List<JugadorFantasy> fuente = disponibles.isEmpty() ? lista : disponibles;

        Collections.shuffle(fuente, random);
        List<JugadorFantasy> resultado = new ArrayList<>();
        for (int i = 0; i < n && i < fuente.size(); i++) {
            resultado.add(fuente.get(i));
        }
        // Si no hay suficientes, completar con cualquiera disponible
        if (resultado.size() < n) {
            throw new RuntimeException("No hay suficientes jugadores de posición " + (lista.isEmpty() ? "desconocida" : lista.get(0).getJugadorReal().getPosicion()));
        }
        return resultado;
    }


    private LigaFantasyResponse cambioTipoRespuesta(LigaFantasy liga){

        return new LigaFantasyResponse(
            liga.getId(),
            liga.getName(),
            liga.getCode(),
            liga.getCreator().getId(),
            liga.getCreator().getUsername(),
            liga.getMaxPlayers()
        );
    }





}
