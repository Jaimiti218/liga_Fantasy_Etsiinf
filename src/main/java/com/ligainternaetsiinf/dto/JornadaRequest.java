package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;

public class JornadaRequest {
    private Integer numero;
    private LocalDateTime fechaInicio;

    public JornadaRequest(){}
    public Integer getNumero(){ return numero; }
    public void setNumero(Integer numero){ this.numero = numero; }
    public LocalDateTime getFechaInicio(){ return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio){ this.fechaInicio = fechaInicio; }
}