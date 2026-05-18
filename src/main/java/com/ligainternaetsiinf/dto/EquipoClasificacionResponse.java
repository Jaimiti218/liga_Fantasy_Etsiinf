package com.ligainternaetsiinf.dto;

public class EquipoClasificacionResponse {
    private Integer id;
    private String nombre;
    private int puntos;
    private int golesAFavor;
    private int golesEnContra;
    private int diferenciaDeGoles;
    private int partidosJugados;

    public EquipoClasificacionResponse(){}

    public EquipoClasificacionResponse(Integer id, String nombre, int puntos,
            int golesAFavor, int golesEnContra, int diferenciaDeGoles, int partidosJugados){
        this.id = id;
        this.nombre = nombre;
        this.puntos = puntos;
        this.golesAFavor = golesAFavor;
        this.golesEnContra = golesEnContra;
        this.diferenciaDeGoles = diferenciaDeGoles;
        this.partidosJugados = partidosJugados;
    }

    public Integer getId(){ return id; }
    public String getNombre(){ return nombre; }
    public int getPuntos(){ return puntos; }
    public int getGolesAFavor(){ return golesAFavor; }
    public int getGolesEnContra(){ return golesEnContra; }
    public int getDiferenciaDeGoles(){ return diferenciaDeGoles; }
    public int getPartidosJugados(){ return partidosJugados; }
}