package com.ligainternaetsiinf.dto;

import java.util.List;

public class EquipoResponse {
    private Integer id;
    private String name;
    private List<String> jugadores; // solo los nombres, sin objetos Equipo dentro

    public EquipoResponse(Integer id, String name, List<String> jugadores){
        this.id = id;
        this.name =name;
        this.jugadores = jugadores;
    }

    public Integer getId(){ return id; }
    public String getName(){ return name; }
    public List<String> getJugadores(){ return jugadores; }
}
