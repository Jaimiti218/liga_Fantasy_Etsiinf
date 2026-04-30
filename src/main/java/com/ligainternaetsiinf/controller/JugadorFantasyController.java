package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.JugadorFantasyResponse;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.service.JugadorFantasyService;

@RestController
@RequestMapping("/jugadores-fantasy")
public class JugadorFantasyController {

    @Autowired
    private JugadorFantasyService jugadorFantasyService;


    @PostMapping("/crear")
    public void crearJugadorFantasy(@RequestParam Integer jugadorRealId){

        jugadorFantasyService.crearJugadorFantasy(jugadorRealId);
    }


    @GetMapping("/{jugadorId}")
    public JugadorFantasyResponse obtenerJugadorFantasy(
            @PathVariable Integer jugadorId){

        return jugadorFantasyService.obtenerJugadorFantasy(jugadorId);
    }


    @GetMapping("/liga/{ligaId}")
    public List<JugadorFantasyResponse> obtenerJugadoresDeLiga(
            @PathVariable Integer ligaId){

        return jugadorFantasyService.obtenerJugadoresSinEquipoFantasy(ligaId);
    }


    @GetMapping("/equipo/{equipoId}")
    public List<JugadorFantasyResponse> obtenerJugadoresDeEquipo(
            @PathVariable Integer equipoId){

        return jugadorFantasyService.obtenerJugadoresDeEquipo(equipoId);
    }


    @PutMapping("/{jugadorId}/clausula")
    public JugadorFantasyResponse subirClausula(@PathVariable Integer jugadorId, @RequestParam long cantidadASubir, @RequestParam int equipoFantasyId){

        return jugadorFantasyService.subirClausula(jugadorId, cantidadASubir, equipoFantasyId);
    }


    @PostMapping("/{jugadorId}/clausulazo")
    public JugadorFantasyResponse ejecutarClausulazo(
            @PathVariable Integer jugadorId,
            @RequestParam Integer equipoCompradorId){

        return jugadorFantasyService.ejecutarClausulazo(jugadorId, equipoCompradorId);
    }

}
