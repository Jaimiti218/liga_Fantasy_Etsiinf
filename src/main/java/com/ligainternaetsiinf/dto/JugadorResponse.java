package com.ligainternaetsiinf.dto;


public class JugadorResponse {
    private Integer id;
    private String fullName;
    private String posicion;
    private String nombreEquipo; // solo el nombre, no el objeto Equipo entero, asi evitamos que se recorra jugador y equipo en bucle

    public JugadorResponse(){}

    public JugadorResponse(Integer id, String fullName, String posicion, String nombreEquipo){
        this.id = id;
        this.fullName = fullName;
        this.posicion = posicion;
        this.nombreEquipo = nombreEquipo;
    }

    public Integer getId(){ return id; }

    public String getFullName(){ return fullName; }
    public String getPosicion(){ return posicion; }

    public String getNombreEquipo(){ return nombreEquipo; }
}
