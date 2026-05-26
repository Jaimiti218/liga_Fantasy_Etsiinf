package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class Jornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer numero;
    private LocalDateTime fechaInicio;  //esta fecha se usara para registrar las alineaciones de esa jornada

    public Jornada(){}

    public Jornada(Integer numero, LocalDateTime fechaInicio){
        this.numero = numero;
        this.fechaInicio = fechaInicio;
    }

    public Integer getId(){ return id; }
    public Integer getNumero(){ return numero; }
    public void setNumero(Integer numero){ this.numero = numero; }
    public LocalDateTime getFechaInicio(){ return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio){ this.fechaInicio = fechaInicio; }
}