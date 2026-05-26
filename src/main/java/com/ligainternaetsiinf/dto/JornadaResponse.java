package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;

public class JornadaResponse {
    private Integer id;
    private Integer numero;
    private LocalDateTime fechaInicio;

    public JornadaResponse(){}
    public JornadaResponse(Integer id, Integer numero, LocalDateTime fechaInicio){
        this.id = id;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
    }
    public Integer getId(){ return id; }
    public Integer getNumero(){ return numero; }
    public LocalDateTime getFechaInicio(){ return fechaInicio; }
}