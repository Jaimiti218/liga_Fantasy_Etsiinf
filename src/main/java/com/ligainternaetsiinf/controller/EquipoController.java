package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ligainternaetsiinf.dto.EquipoResponse;
import com.ligainternaetsiinf.dto.EquipoUpdateDTO;
import com.ligainternaetsiinf.model.Equipo;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.repository.EquipoRepository;
import com.ligainternaetsiinf.service.EquipoService;

@RestController
@RequestMapping("/equipos")
public class EquipoController {
    @Autowired
    private EquipoService equipoService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Equipo crearEquipo(@RequestBody Equipo equipo){
        return equipoService.crearEquipo(equipo);
    }

    @GetMapping
    public List<EquipoResponse> listarEquipos(){
        return equipoService.listarEquipos();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Equipo editarEquipo(@PathVariable Integer id, @RequestBody EquipoUpdateDTO dto){

        return equipoService.editarEquipo(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminarEquipo(@PathVariable Integer id){

        equipoService.eliminarEquipo(id);
    }
}
