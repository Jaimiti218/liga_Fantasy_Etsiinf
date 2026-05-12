package com.ligainternaetsiinf.dto;

public class PuntosJornadaJugadorResponse {
    private Integer jornada;
    private int puntos;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;
    private boolean jugo;

    public PuntosJornadaJugadorResponse(){}

    public PuntosJornadaJugadorResponse(Integer jornada, int puntos, int goles,
            int asistencias, int tarjetasAmarillas, int tarjetasRojas,
            int paradas, boolean jugo){
        this.jornada = jornada;
        this.puntos = puntos;
        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.paradas = paradas;
        this.jugo = jugo;
    }

    public Integer getJornada(){ return jornada; }
    public int getPuntos(){ return puntos; }
    public int getGoles(){ return goles; }
    public int getAsistencias(){ return asistencias; }
    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public int getTarjetasRojas(){ return tarjetasRojas; }
    public int getParadas(){ return paradas; }
    public boolean isJugo(){ return jugo; }
}
