package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.VariacionPrecioResponse;
import com.ligainternaetsiinf.service.PrecioJugadorService;

@RestController
@RequestMapping("/precios")
public class PrecioController {

    @Autowired
    private PrecioJugadorService precioJugadorService;

    @GetMapping("/subidas")
    public List<VariacionPrecioResponse> subidas(
            @RequestParam(defaultValue = "semana") String periodo) {
        return precioJugadorService.obtenerVariaciones(periodo, true);
    }

    @GetMapping("/bajadas")
    public List<VariacionPrecioResponse> bajadas(
            @RequestParam(defaultValue = "semana") String periodo) {
        return precioJugadorService.obtenerVariaciones(periodo, false);
    }
}