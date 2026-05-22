package com.ligainternaetsiinf.dto;

public class VariacionPrecioResponse {
    private Integer jugadorId;
    private String nombre;
    private String posicion;
    private String equipo;
    private long precioAnterior;
    private long precioActual;
    private long diferencia;

    public VariacionPrecioResponse(){}

    public VariacionPrecioResponse(Integer jugadorId, String nombre, String posicion,
            String equipo, long precioAnterior, long precioActual, long diferencia){
        this.jugadorId     = jugadorId;
        this.nombre        = nombre;
        this.posicion      = posicion;
        this.equipo        = equipo;
        this.precioAnterior = precioAnterior;
        this.precioActual   = precioActual;
        this.diferencia     = diferencia;
    }

    public Integer getJugadorId(){ return jugadorId; }
    public String getNombre(){ return nombre; }
    public String getPosicion(){ return posicion; }
    public String getEquipo(){ return equipo; }
    public long getPrecioAnterior(){ return precioAnterior; }
    public long getPrecioActual(){ return precioActual; }
    public long getDiferencia(){ return diferencia; }
}
