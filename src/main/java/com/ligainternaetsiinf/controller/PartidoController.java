package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.EstadisticasPartidoResponse;
import com.ligainternaetsiinf.dto.PartidoRequest;
import com.ligainternaetsiinf.dto.PartidoResponse;
import com.ligainternaetsiinf.dto.ResultadoRequest;
import com.ligainternaetsiinf.service.PartidoService;

@RestController
@RequestMapping("/partidos")
public class PartidoController {

    @Autowired
    private PartidoService partidoService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public PartidoResponse crearPartido(@RequestBody PartidoRequest request) {
        return partidoService.crearPartido(request);
    }

    @GetMapping
    public List<PartidoResponse> listarPartidos() {
        return partidoService.listarPartidos();
    }

    @GetMapping("/jornada/{jornada}")
    public List<PartidoResponse> listarPorJornada(@PathVariable Integer jornada) {
        return partidoService.listarPartidosPorJornada(jornada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public PartidoResponse editarPartido(@PathVariable Integer id,
            @RequestBody PartidoRequest request) {
        return partidoService.editarPartido(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminarPartido(@PathVariable Integer id) {
        partidoService.eliminarPartido(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/resultado")
    public PartidoResponse registrarResultado(
            @PathVariable Integer id,
            @RequestBody ResultadoRequest request) {
        return partidoService.registrarResultado(id, request);
    }

    @GetMapping("/{id}/estadisticas")
    public List<EstadisticasPartidoResponse> getEstadisticasPartido(@PathVariable Integer id) {
        return partidoService.getEstadisticasPartido(id);
    }
}
