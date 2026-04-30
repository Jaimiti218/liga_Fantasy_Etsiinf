package com.ligainternaetsiinf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.ClasificacionResponse;
import com.ligainternaetsiinf.dto.EquipoFantasyResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyResponse;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;

@Service
public class EquipoFantasyService {

    @Autowired
    private EquipoFantasyRepository equipoFantasyRepository;

    public EquipoFantasyResponse obtenerEquipo(Integer equipoId){

        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        return cambiarTipoRespuesta(equipo);
    }

    public List<EquipoFantasyResponse> obtenerEquiposDeLiga(Integer ligaId){

        List<EquipoFantasy> equipos = equipoFantasyRepository.findByLigaFantasyId(ligaId);

        List<EquipoFantasyResponse> resultado = new ArrayList<>();

        for(EquipoFantasy ef : equipos){
            resultado.add(cambiarTipoRespuesta(ef));
        }

        return resultado;
    }

    /*creamos lo de clasificacion response para que en el frontend no se vean datos innecesarios */
    public List<ClasificacionResponse> obtenerClasificacionLiga(Integer ligaId){

        List<EquipoFantasy> equipos = equipoFantasyRepository.findByLigaFantasyIdOrderByPuntosDesc(ligaId);

        List<ClasificacionResponse> resultado = new ArrayList<>();

        for(EquipoFantasy ef : equipos){
            resultado.add(new ClasificacionResponse(ef.getId(), ef.getUser().getUsername(), ef.getPuntos()));
        }

        return resultado;
    }

    public void eliminarEquipo(Integer equipoId){

        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        equipoFantasyRepository.delete(equipo);
    }

    private EquipoFantasyResponse cambiarTipoRespuesta(EquipoFantasy equipo){

        List<String> jugadores = new ArrayList<>();

        for(JugadorFantasy jf : equipo.getJugadores()){
            jugadores.add(jf.getJugadorReal().getFullName());
        }

        return new EquipoFantasyResponse(
                equipo.getId(),
                equipo.getLigaFantasy().getId(),
                equipo.getLigaFantasy().getName(),
                equipo.getUser().getId(),
                equipo.getUser().getUsername(),
                equipo.getDinero(),
                equipo.getPuntos(),
                jugadores
        );
    }
}
