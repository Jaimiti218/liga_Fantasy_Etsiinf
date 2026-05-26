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
    private int golesEncajados;
    private String posicionJugada;   // posición usada en ese partido
    private String posicionDefecto;  // posición habitual del jugador

    public PuntosJornadaJugadorResponse(){}

    public PuntosJornadaJugadorResponse(Integer jornada, int puntos, int goles,
            int asistencias, int tarjetasAmarillas, int tarjetasRojas,
            int paradas, boolean jugo, int golesEncajados,String posicionJugada, String posicionDefecto){
        this.jornada = jornada;
        this.puntos = puntos;
        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.paradas = paradas;
        this.jugo = jugo;
        this.golesEncajados = golesEncajados;
        this.posicionJugada  = posicionJugada;
        this.posicionDefecto = posicionDefecto;
    }

    public Integer getJornada(){ return jornada; }
    public int getPuntos(){ return puntos; }
    public int getGoles(){ return goles; }
    public int getAsistencias(){ return asistencias; }
    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public int getTarjetasRojas(){ return tarjetasRojas; }
    public int getParadas(){ return paradas; }
    public boolean isJugo(){ return jugo; }
    public int getGolesEncajados(){ return golesEncajados; }
    public String getPosicionJugada(){ return posicionJugada; }
    public String getPosicionDefecto(){ return posicionDefecto; }
}
