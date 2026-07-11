package com.ligainternaetsiinf.dto;

public class MiEquipoResumenResponse {
    private Integer equipoId;
    private int puntos;
    private long dinero;
    private int posicion;
    private boolean tieneAviso;

    public MiEquipoResumenResponse(Integer equipoId, int puntos, long dinero, int posicion, boolean tieneAviso) {
        this.equipoId = equipoId;
        this.puntos = puntos;
        this.dinero = dinero;
        this.posicion = posicion;
        this.tieneAviso = tieneAviso;
    }
   

    public Integer getEquipoId() { return equipoId; }
    public int getPuntos() { return puntos; }
    public long getDinero() { return dinero; }
    public int getPosicion() { return posicion; }
    public boolean isTieneAviso() { return tieneAviso; }
}
