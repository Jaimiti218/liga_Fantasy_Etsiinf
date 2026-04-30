package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ligainternaetsiinf.dto.LigaFantasyResponse;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.service.LigaFantasyService;


@RestController
@RequestMapping("/ligas-fantasy")
public class LigaFantasyController {

    @Autowired
    private LigaFantasyService ligaFantasyService;

    @PostMapping("/crear")
    public LigaFantasyResponse crearLiga(@RequestParam String nombre, @RequestParam Integer userId){ /*de momento no hace falta crear un DTO, pero si más adelante 
                                                                                            añado mas campos, si */
        /*el userId lo va a mandar el frontend sin necesidad de que el usuario escriba su id,
        ya que el sistema ya va a saber quién es el que está logueado */
        return ligaFantasyService.crearLiga(nombre, userId);
    }

    // unirse a liga
    @PostMapping("/unirse")
    public LigaFantasyResponse unirseALiga(@RequestParam String code, @RequestParam Integer userId){
        return ligaFantasyService.unirseALiga(code, userId);
    }

    // obtener liga por id
    @GetMapping("/{ligaId}")
    public LigaFantasyResponse obtenerLiga(@PathVariable Integer ligaId){

        return ligaFantasyService.obtenerLiga(ligaId);
    }

    @GetMapping("/usuario/{userId}")
    public List<LigaFantasyResponse> obtenerLigasDeUsuario(@PathVariable Integer userId){
        return ligaFantasyService.obtenerLigasDeUsuario(userId);
    }

    // abandonar liga
    @DeleteMapping("/{ligaId}/abandonar")
    public void abandonarLiga(@PathVariable Integer ligaId, @RequestParam Integer userId){

        ligaFantasyService.abandonarLiga(ligaId, userId);
    }
}
