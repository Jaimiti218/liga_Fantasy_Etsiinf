package com.ligainternaetsiinf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/fantasy/auth")
    public String authPage() {
        return "fantasy/auth"; // busca templates/fantasy/auth.html
    }

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/admin")
    public String adminPanel() {
        return "admin/panel";
    }

    @GetMapping("/admin/jugadores")
    public String adminJugadores() {
        return "admin/jugadores";
    }

    @GetMapping("/admin/equipos")
    public String adminEquipos() {
        return "admin/equipos";
    }

    @GetMapping("/fantasy/mis-ligas")
    public String misLigas() {
        return "fantasy/mis-ligas";
    }

    @GetMapping("/fantasy/liga/{id}")
    public String ligaFantasy() {
        return "fantasy/liga";
    }
    
    @GetMapping("/fantasy/plantilla/{equipoId}")
    public String plantilla() {
        return "fantasy/plantilla";
    }

    @GetMapping("/fantasy/mercado/{ligaId}")
    public String mercado() {
        return "fantasy/mercado";
    }

    @GetMapping("/fantasy/equipo/{equipoId}")
    public String verEquipo() {
        return "fantasy/equipo-otro";
    }
}
