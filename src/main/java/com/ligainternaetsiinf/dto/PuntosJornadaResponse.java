package com.ligainternaetsiinf.dto;

public class PuntosJornadaResponse {
    private Integer jugadorFantasyId;
    private String nombre;
    private String posicion;
    private int puntos;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;
    private boolean jugo;

    public PuntosJornadaResponse(){}

    public PuntosJornadaResponse(Integer jugadorFantasyId, String nombre, String posicion,
            int puntos, int goles, int asistencias, int tarjetasAmarillas,
            int tarjetasRojas, int paradas, boolean jugo){
        this.jugadorFantasyId = jugadorFantasyId;
        this.nombre = nombre;
        this.posicion = posicion;
        this.puntos = puntos;
        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.paradas = paradas;
        this.jugo = jugo;
    }

    public Integer getJugadorFantasyId(){ return jugadorFantasyId; }
    public String getNombre(){ return nombre; }
    public String getPosicion(){ return posicion; }
    public int getPuntos(){ return puntos; }
    public int getGoles(){ return goles; }
    public int getAsistencias(){ return asistencias; }
    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public int getTarjetasRojas(){ return tarjetasRojas; }
    public int getParadas(){ return paradas; }
    public boolean isJugo(){ return jugo; }
}
