package com.ligainternaetsiinf.dto;


public class JugadorResponse {
    private Integer id;
    private String fullName;
    private Boolean esPortero;
    private String nombreEquipo; // solo el nombre, no el objeto Equipo entero, asi evitamos que se recorra jugador y equipo en bucle

    public JugadorResponse(){}

    public JugadorResponse(Integer id, String fullName, Boolean esPortero, String nombreEquipo){
        this.id = id;
        this.fullName = fullName;
        this.esPortero = esPortero;
        this.nombreEquipo = nombreEquipo;
    }

    public Integer getId(){ return id; }

    public String getFullName(){ return fullName; }
    public Boolean getEsPortero(){ return esPortero; }

    public String getNombreEquipo(){ return nombreEquipo; }
}
