package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Mercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private LigaFantasy ligaFantasy;

    private LocalDateTime horaCreacionMercado;

    public Mercado(){}

    public Mercado(LigaFantasy ligaFantasy){
        this.ligaFantasy = ligaFantasy;
        //this.jugadoresDisponibles = jugadoresDisponibles;
        this.horaCreacionMercado = LocalDateTime.now();
    }

    public Integer getId(){
        return id;
    }

    public LigaFantasy getLiga(){
        return ligaFantasy;
    }

    public void setLiga(LigaFantasy ligaFantasy){
        this.ligaFantasy = ligaFantasy;
    }

    /*public List<JugadorFantasy> getJugadoresDisponibles(){
        return jugadoresDisponibles;
    }

    public void setJugadoresDisponibles(List<JugadorFantasy> jugadoresDisponibles){
        this.jugadoresDisponibles = jugadoresDisponibles;
    }*/

    public LocalDateTime getHoraCreacionMercado(){
        return horaCreacionMercado;
    }

}