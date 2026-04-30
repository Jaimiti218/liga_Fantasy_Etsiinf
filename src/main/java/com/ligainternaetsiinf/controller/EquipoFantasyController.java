package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.ClasificacionResponse;
import com.ligainternaetsiinf.dto.EquipoFantasyResponse;
import com.ligainternaetsiinf.service.EquipoFantasyService;


@RestController
@RequestMapping("/equipos-fantasy")
public class EquipoFantasyController {

    @Autowired
    private EquipoFantasyService equipoFantasyService;

    @GetMapping("/{equipoId}")
    public EquipoFantasyResponse obtenerEquipo(@PathVariable Integer equipoId){
        return equipoFantasyService.obtenerEquipo(equipoId);
    }

    /*creamos lo de clasificacion response para que en el frontend no se vean datos innecesarios */
    @GetMapping("/ligas/{ligaId}/clasificacion")
    public List<ClasificacionResponse> obtenerClasificacionLiga(@PathVariable Integer ligaId) {
        return equipoFantasyService.obtenerClasificacionLiga(ligaId);
    }
    

    @GetMapping("/liga/{ligaId}")
    public List<EquipoFantasyResponse> obtenerEquiposDeLiga(@PathVariable Integer ligaId){
        return equipoFantasyService.obtenerEquiposDeLiga(ligaId);
    }

    @DeleteMapping("/{equipoId}")
    public void eliminarEquipo(@PathVariable Integer equipoId){
        equipoFantasyService.eliminarEquipo(equipoId);
    }
}
