package com.ligainternaetsiinf.controller;

import java.util.List;
import org.springframework.security.core.Authentication;

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
import com.ligainternaetsiinf.security.CustomUserDetails;
import com.ligainternaetsiinf.service.LigaFantasyService;


@RestController
@RequestMapping("/ligas-fantasy")
public class LigaFantasyController {

    @Autowired
    private LigaFantasyService ligaFantasyService;

    @PostMapping("/crear")
    public LigaFantasyResponse crearLiga(@RequestParam String nombre, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ligaFantasyService.crearLiga(nombre, userDetails.getId());
    }

    @PostMapping("/unirse")
    public LigaFantasyResponse unirseALiga(@RequestParam String code, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ligaFantasyService.unirseALiga(code, userDetails.getId());
    }

    @GetMapping("/{ligaId}")
    public LigaFantasyResponse obtenerLiga(@PathVariable Integer ligaId) {
        return ligaFantasyService.obtenerLiga(ligaId);
    }

    @GetMapping("/usuario/mis-ligas")
    public List<LigaFantasyResponse> obtenerLigasDeUsuario(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ligaFantasyService.obtenerLigasDeUsuario(userDetails.getId());
    }

    @DeleteMapping("/{ligaId}/abandonar")
    public void abandonarLiga(@PathVariable Integer ligaId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ligaFantasyService.abandonarLiga(ligaId, userDetails.getId());
    }

}
