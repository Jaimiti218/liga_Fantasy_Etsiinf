package com.ligainternaetsiinf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.JugadorFantasyResponse;
import com.ligainternaetsiinf.dto.JugadorResponse;
import com.ligainternaetsiinf.dto.JugadorUpdateDTO;
import com.ligainternaetsiinf.model.Equipo;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.repository.EquipoRepository;
import com.ligainternaetsiinf.repository.JugadorRepository;

@Service
public class JugadorService {
    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    public Jugador crearJugador(Jugador jugador){
        if(jugadorRepository.findByFullName(jugador.getFullName()).isPresent()){
            throw new RuntimeException("Ya existe este jugador!");
        }

        Optional<Equipo> aux = equipoRepository.findByName(jugador.getEquipo().getName());

        if(!aux.isPresent()){
            throw new RuntimeException("No existe ese equipo");
        }
        return jugadorRepository.save(new Jugador(jugador.getFullName(), aux.get(), jugador.getEsPortero()));
    }

    public List<JugadorResponse> listarJugadores(){
        List<Jugador> aux = jugadorRepository.findAll();

        List<JugadorResponse> resultado = new ArrayList<>();

        for(Jugador jugador : aux){
            resultado.add(cambioTipoRespuesta(jugador));
        }

        return resultado;


    }

    public Jugador editarJugador(Integer id, JugadorUpdateDTO jugadorActualizado){  /*Este metodo es para cambiar los datos esenciales del jugador, 
        en principio las estadisticas no, eso se  modificará cuando se introduzcan los datos de los partidos en los que este jugador haya participado,
        ahi se le sumaran las estadisticas que haya tenido */
        Optional<Jugador> aux = jugadorRepository.findById(id);
        if(!aux.isPresent()){
            throw new RuntimeException("No se ha encontrado al jugador " + jugadorActualizado.getFullName());
        }
        Jugador jugador = aux.get();
        if(jugadorActualizado.getNombreEquipo() != null){
            Optional<Equipo> aux2 = equipoRepository.findByName(jugadorActualizado.getNombreEquipo());
            
            if(!aux2.isPresent()){
                throw new RuntimeException("No existe ese equipo");
            }
            Equipo equipo = aux2.get();

            jugador.setEquipo(equipo);
            equipo.getJugadores().add(jugador);

        }
        if(jugadorActualizado.getEsPortero() != null){
            jugador.setEsPortero(jugadorActualizado.getEsPortero());
        }
        
        if(jugadorActualizado.getFullName() != null){
            jugador.setFullName(jugadorActualizado.getFullName());
        }

        return jugadorRepository.save(jugador);
    }

    public void eliminarJugador(Integer id){

        if(!jugadorRepository.existsById(id)){
            throw new RuntimeException("Jugador no encontrado");
        }

        jugadorRepository.deleteById(id);
    }

    private JugadorResponse cambioTipoRespuesta(Jugador jugador){ /*creamos este metodo ya que el cambio del tipo de dato lo hacemos mucho */

        String nombreEquipo = null;

        if(jugador.getEquipo() != null){
            nombreEquipo = jugador.getEquipo().getName();
        }

        return new JugadorResponse(jugador.getId(), jugador.getFullName(), jugador.getEsPortero(), nombreEquipo);
    }
}
