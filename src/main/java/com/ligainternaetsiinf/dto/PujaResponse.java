package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;

public class PujaResponse {
    private Integer id;
    private Integer jugadorFantasyId;
    private String jugadorNombre;
    private String posicion;
    private long valorMercado;
    private String equipoReal;
    private long cantidad;
    private LocalDateTime fecha;
    private String duenoNombre;  // dueño del jugador (vendedor)
    private String ofertante;    // quien hace la puja (comprador, o "La Liga")

    public PujaResponse(){}

    public PujaResponse(Integer id, Integer jugadorFantasyId, String jugadorNombre,
            String posicion, long valorMercado, String equipoReal,
            long cantidad, LocalDateTime fecha, String duenoNombre, String ofertante){
        this.id = id;
        this.jugadorFantasyId = jugadorFantasyId;
        this.jugadorNombre = jugadorNombre;
        this.posicion = posicion;
        this.valorMercado = valorMercado;
        this.equipoReal = equipoReal;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.duenoNombre = duenoNombre;
        this.ofertante = ofertante;
    }

    public Integer getId(){ return id; }
    public Integer getJugadorFantasyId(){ return jugadorFantasyId; }
    public String getJugadorNombre(){ return jugadorNombre; }
    public String getPosicion(){ return posicion; }
    public long getValorMercado(){ return valorMercado; }
    public String getEquipoReal(){ return equipoReal; }
    public long getCantidad(){ return cantidad; }
    public LocalDateTime getFecha(){ return fecha; }
    public String getDuenoNombre(){ return duenoNombre; }
    public String getOfertante(){ return ofertante; }
}