package com.ligainternaetsiinf.dto;

public class EstadisticasPartidoResponse {
    private Integer jugadorId;
    private String jugadorNombre;
    private String posicion;
    private String equipoNombre;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;
    private boolean jugo;

    public EstadisticasPartidoResponse(){}

    public EstadisticasPartidoResponse(Integer jugadorId, String jugadorNombre,
            String posicion, String equipoNombre, int goles, int asistencias,
            int tarjetasAmarillas, int tarjetasRojas, int paradas, boolean jugo){
        this.jugadorId = jugadorId;
        this.jugadorNombre = jugadorNombre;
        this.posicion = posicion;
        this.equipoNombre = equipoNombre;
        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.paradas = paradas;
        this.jugo = jugo;
    }

    public Integer getJugadorId(){ return jugadorId; }
    public String getJugadorNombre(){ return jugadorNombre; }
    public String getPosicion(){ return posicion; }
    public String getEquipoNombre(){ return equipoNombre; }
    public int getGoles(){ return goles; }
    public int getAsistencias(){ return asistencias; }
    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public int getTarjetasRojas(){ return tarjetasRojas; }
    public int getParadas(){ return paradas; }
    public boolean isJugo(){ return jugo; }
}