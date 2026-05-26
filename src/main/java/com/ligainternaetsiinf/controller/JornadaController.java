package com.ligainternaetsiinf.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ligainternaetsiinf.dto.JornadaRequest;
import com.ligainternaetsiinf.dto.JornadaResponse;
import com.ligainternaetsiinf.service.JornadaService;

@RestController
@RequestMapping("/jornadas")
public class JornadaController {

    @Autowired private JornadaService jornadaService;

    @GetMapping
    public List<JornadaResponse> listar() {
        return jornadaService.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public JornadaResponse crear(@RequestBody JornadaRequest request) {
        return jornadaService.crear(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public JornadaResponse editar(@PathVariable Integer id, @RequestBody JornadaRequest request) {
        return jornadaService.editar(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        jornadaService.eliminar(id);
    }
}