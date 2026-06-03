package com.ligainternaetsiinf.dto;

public class ClasificacionResponse {
    private Integer equipoId;
    private String username;
    private int puntos;
    private String fotoPerfil; 
    private long valorPlantilla;

    public ClasificacionResponse(Integer equipoId, String username, int puntos, String fotoPerfil, long valorPlantilla){
        this.equipoId = equipoId;
        this.username = username;
        this.puntos = puntos;
        this.fotoPerfil = fotoPerfil;
        this.valorPlantilla = valorPlantilla;
    }

    public Integer getEquipoId(){ return equipoId; }
    public String getUsername(){ return username; }
    public int getPuntos(){ return puntos; }
    public String getFotoPerfil(){ return fotoPerfil; }
    public long getValorPlantilla(){ return valorPlantilla; }

    
}
