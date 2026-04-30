package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ligainternaetsiinf.dto.JugadorUpdateDTO;
import com.ligainternaetsiinf.dto.JugadorResponse;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.service.JugadorService;

@RestController
@RequestMapping("/jugadores")
public class JugadorController {
    @Autowired
    private JugadorService jugadorService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Jugador crearJugador(@RequestBody Jugador jugador){
        return jugadorService.crearJugador(jugador);
    }

    @GetMapping
    public List<JugadorResponse> listarJugadores(){
        return jugadorService.listarJugadores();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Jugador editarJugador(@PathVariable Integer id, @RequestBody JugadorUpdateDTO dto){

        return jugadorService.editarJugador(id, dto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminarJugador(@PathVariable Integer id){

        jugadorService.eliminarJugador(id);
    }
}
