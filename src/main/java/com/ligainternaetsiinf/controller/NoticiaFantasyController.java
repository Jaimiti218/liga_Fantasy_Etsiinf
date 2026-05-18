package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.NoticiaResponse;
import com.ligainternaetsiinf.security.CustomUserDetails;
import com.ligainternaetsiinf.service.NoticiaFantasyService;

@RestController
@RequestMapping("/noticias")
public class NoticiaFantasyController {

    @Autowired
    private NoticiaFantasyService noticiaService;

    @GetMapping("/liga/{ligaId}")
    public List<NoticiaResponse> obtenerNoticias(
            @PathVariable Integer ligaId,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        return noticiaService.obtenerNoticias(ligaId, ud.getId());
    }
}
