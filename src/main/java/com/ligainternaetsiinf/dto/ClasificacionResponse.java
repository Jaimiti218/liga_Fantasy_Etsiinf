package com.ligainternaetsiinf.dto;

public class ClasificacionResponse {
    private Integer equipoId;
    private String username;
    private int puntos;
    private String fotoPerfil; // ← añadir

    public ClasificacionResponse(Integer equipoId, String username, int puntos, String fotoPerfil){
        this.equipoId = equipoId;
        this.username = username;
        this.puntos = puntos;
        this.fotoPerfil = fotoPerfil;
    }

    public Integer getEquipoId(){ return equipoId; }
    public String getUsername(){ return username; }
    public int getPuntos(){ return puntos; }
    public String getFotoPerfil(){ return fotoPerfil; }

    
}
