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

        for(Jugador j : jugadoresReales){

            JugadorFantasy jf = new JugadorFantasy(j, liga);

            jugadorFantasyRepository.save(jf);
        }
        
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

        equipoFantasyRepository.delete(equipo);
    }




    private List<JugadorFantasy> asignarEquipoInicial(LigaFantasy liga){
        /*TENER EN CUENTA: este metodo esta pensado teniendo en cuenta que los jugadores de campo no tienen posicion concreta
    (defensa, mediocentro o delantero), si mas adelante eso se añade, habra que modificar un poco este metodo */

        List<JugadorFantasy> disponibles = jugadorFantasyRepository.findByLigaFantasyIdAndEquipoFantasyIsNull(liga.getId());
        List<JugadorFantasy> porteros = new ArrayList<>();
        List<JugadorFantasy> campo = new ArrayList<>();
        List<JugadorFantasy> plantilla = new ArrayList<>();
        
        for(JugadorFantasy jf : disponibles){

            if(jf.getJugadorReal().getEsPortero()){
                porteros.add(jf);
            }
            else{
                campo.add(jf);
            }
        }

        Random random = new Random();

        long valorTotal = 0;

        /*elegir portero equilibrado*/
        List<JugadorFantasy> porterosPrecioNormal = new ArrayList<>(); /*esto será basicamente para porteros con precios mas cercanos a la media,
        para crear plantillas iniciales equilibradas en la medida de lo posible, porque si de primeras el juego te da al mejor portero de la liga, 
        que vale 30M, sería injusto. No obstante */
        List<JugadorFantasy> restoPorteros = new ArrayList<>(); /*este se usa basicamente para porteros o con mucho precio, o con muy poco, que en un
        principio se evitará dar, pero si hay muchos jugadores en esa liga fantasy, pues no queda otra (sobretodo con los porteros) */

        for(JugadorFantasy p : porteros){

            if(p.getJugadorReal().getValorMercado() <= 15000000 && p.getJugadorReal().getValorMercado() >= 2000000){ /*estos dos valores son susceptibles de cambio */
                porterosPrecioNormal.add(p);
            }
            else{
                restoPorteros.add(p);
            }
        }
        JugadorFantasy porteroEquipoInicial = new JugadorFantasy();
        if(porterosPrecioNormal.size()!=0){
            porteroEquipoInicial = porterosPrecioNormal.get(random.nextInt(porterosPrecioNormal.size()));
        }
        else{
            if(restoPorteros.stream().min(Comparator.comparing(j -> j.getJugadorReal().getValorMercado())).isPresent()){
                porteroEquipoInicial = restoPorteros.stream().min(Comparator.comparing(j -> j.getJugadorReal().getValorMercado())).get();
            }
           
        }
    
        plantilla.add(porteroEquipoInicial);
        valorTotal += porteroEquipoInicial.getJugadorReal().getValorMercado();

        // elegir jugadores de campo
        Collections.shuffle(campo);

        JugadorFantasy aux = new JugadorFantasy(); /*esta variable se usa para rellenar la plantilla en caso de que se recorra TODA la lista de jugadores
        de la liga y no haya sido posible crear una plantilla equilibrada, entonces habra que coger jugadores random */

        for(int i=0; i<campo.size() ; i++){

            JugadorFantasy j = campo.get(i);

            if(plantilla.size() == 7){
                break;
            }

            long valor = j.getJugadorReal().getValorMercado();

            if(valor < 6000000 || valor > 20000000){
                if(i == campo.size()-1){ /*esto significa que es el ULTIMO jugador de la lista */
                    while(plantilla.size() < 7){                       
                        aux = campo.get(random.nextInt(campo.size()));/*obtenemos un jugador aleatorio, con el precio que sea, ya que mejor dar un jugador
                                                                    con mucho o poco valor y que la plantilla quede un poco descompensada, a que no haya plantilla completa*/
                        if(!plantilla.contains(aux)){ /*comprobamos que el que se ha cogido no estuviese JUSTO ya en la plantilla */
                            plantilla.add(aux);
                            valorTotal += valor;
                        }
                    }
                    continue;                   
                }
                else continue;
                
            }

            plantilla.add(j);
            valorTotal += valor;

            if(i == campo.size()-1 && plantilla.size() < 7){ /*tenemos que repetirlo aqui por si acaso justamente el ultimo jugador fuese entre 6-20M
                , caso en el que no se rellenaria bien la plantilla, porque el codigo de arriba para ello con jugadores random no se ejecutaria */
                while(plantilla.size() < 7){                       
                    aux = campo.get(random.nextInt(campo.size())); 
                    if(!plantilla.contains(aux)){ 
                        plantilla.add(aux);
                        valorTotal += valor;
                     }
                }
                continue;                   
            }
        }

        if(plantilla.size() < 7){
            throw new RuntimeException("No se pudo generar plantilla equilibrada");
        }

        return plantilla;
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
