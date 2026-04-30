package com.ligainternaetsiinf.model;

import jakarta.persistence.*;

@Entity
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fullName;

    @ManyToOne
    private Equipo equipo;

    private boolean esPortero;

    private int partidosJugados;
    private long valorMercado;
    private int puntosFantasy;

    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;

    public Jugador(){}

    public Jugador(String fullName, Equipo equipo, boolean esPortero){
        this.fullName = fullName;
        this.equipo = equipo;
        this.esPortero = esPortero;
        this.valorMercado = 10000000; /*10M será el precio inicial para todo jugador, que luego variará según su rendimiento y demás */
        this.puntosFantasy = 0;
        this.goles = 0;
        this.asistencias = 0;
        this.tarjetasAmarillas = 0;
        this.tarjetasRojas = 0;
        this.paradas = 0;
        this.partidosJugados = 0;
    }

    public Integer getId(){ return id; }

    public String getFullName(){ return fullName; }

    public void setFullName(String fullName){ this.fullName = fullName; }

    public Equipo getEquipo(){ return equipo; }

    public void setEquipo(Equipo equipo){ this.equipo = equipo; }

    public boolean getEsPortero(){ return esPortero; }

    public void setEsPortero(boolean esPortero){ this.esPortero = esPortero; }

    public long getValorMercado(){
        return valorMercado;
    }
}