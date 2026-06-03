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

    private String formacion;

    private String fotoPerfil;

    private long valorPlantilla;

    public EquipoFantasyResponse(){}

    public EquipoFantasyResponse(Integer id, Integer ligaId, String ligaNombre, Integer userId, String userNombre, long dinero, int puntos, 
        List<String> jugadores, String formacion, String fotoPerfil, long valorPlantilla){

        this.id = id;
        this.ligaId = ligaId;
        this.ligaNombre = ligaNombre;
        this.userId = userId;
        this.userNombre = userNombre;
        this.dinero = dinero;
        this.puntos = puntos;
        this.jugadores = jugadores;
        this.formacion = formacion;
        this.fotoPerfil = fotoPerfil;
        this.valorPlantilla = valorPlantilla;
    }

    public Integer getId(){ return id; }

    public Integer getLigaId(){ return ligaId; }

    public String getLigaNombre(){ return ligaNombre; }

    public Integer getUserId(){ return userId; }

    public String getUserNombre(){ return userNombre; }

    public long getDinero(){ return dinero; }

    public int getPuntos(){ return puntos; }

    public List<String> getJugadores(){ return jugadores; }
    public String getFormacion(){ return formacion; }

    public String getFotoPerfil(){ return fotoPerfil; }
    public long getValorPlantilla(){ return valorPlantilla; }
}
