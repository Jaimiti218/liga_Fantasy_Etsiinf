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
}
