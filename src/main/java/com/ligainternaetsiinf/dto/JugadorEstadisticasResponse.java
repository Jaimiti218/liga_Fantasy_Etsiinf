package com.ligainternaetsiinf.dto;

public class JugadorEstadisticasResponse {
    private Integer id;
    private String nombre;
    private String posicion;
    private String equipo;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;
    private int puntosFantasy;
    private int partidosJugados;

    public JugadorEstadisticasResponse(){}

    public JugadorEstadisticasResponse(Integer id, String nombre, String posicion,
            String equipo, int goles, int asistencias, int tarjetasAmarillas,
            int tarjetasRojas, int paradas, int puntosFantasy, int partidosJugados){
        this.id = id;
        this.nombre = nombre;
        this.posicion = posicion;
        this.equipo = equipo;
        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.paradas = paradas;
        this.puntosFantasy = puntosFantasy;
        this.partidosJugados = partidosJugados;
    }

    public Integer getId(){ return id; }
    public String getNombre(){ return nombre; }
    public String getPosicion(){ return posicion; }
    public String getEquipo(){ return equipo; }
    public int getGoles(){ return goles; }
    public int getAsistencias(){ return asistencias; }
    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public int getTarjetasRojas(){ return tarjetasRojas; }
    public int getParadas(){ return paradas; }
    public int getPuntosFantasy(){ return puntosFantasy; }
    public int getPartidosJugados(){ return partidosJugados; }
}
