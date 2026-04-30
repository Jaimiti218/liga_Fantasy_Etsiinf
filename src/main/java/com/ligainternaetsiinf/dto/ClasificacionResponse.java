package com.ligainternaetsiinf.dto;

public class ClasificacionResponse {
    private Integer equipoId;
    private String userNombre;
    private int puntos;

    public ClasificacionResponse(){}

    public ClasificacionResponse(Integer equipoId, String userNombre, int puntos){
        this.equipoId = equipoId;
        this.userNombre = userNombre;
        this.puntos = puntos;
    }

    public Integer getEquipoId(){
        return equipoId;
    }

    public String getUserNombre(){
        return userNombre;
    }

    public int getPuntos(){
        return puntos;
    }
    
}
