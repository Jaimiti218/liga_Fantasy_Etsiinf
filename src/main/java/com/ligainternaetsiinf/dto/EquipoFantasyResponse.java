package com.ligainternaetsiinf.dto;

import java.util.List;

public class EquipoFantasyResponse {

    private Integer id;

    private Integer ligaId;
    private String ligaNombre;
    private Integer userId;

    private String userNombre;

    private long dinero;
    
    private int puntos;

    private List<String> jugadores;

    public EquipoFantasyResponse(){}

    public EquipoFantasyResponse(Integer id, Integer ligaId, String ligaNombre, Integer userId, String userNombre, long dinero, int puntos, 
        List<String> jugadores){

        this.id = id;
        this.ligaId = ligaId;
        this.ligaNombre = ligaNombre;
        this.userId = userId;
        this.userNombre = userNombre;
        this.dinero = dinero;
        this.puntos = puntos;
        this.jugadores = jugadores;
    }

    public Integer getId(){ return id; }

    public Integer getLigaId(){ return ligaId; }

    public String getLigaNombre(){ return ligaNombre; }

    public Integer getUserId(){ return userId; }

    public String getUserNombre(){ return userNombre; }

    public long getDinero(){ return dinero; }

    public int getPuntos(){ return puntos; }

    public List<String> getJugadores(){ return jugadores; }
}
