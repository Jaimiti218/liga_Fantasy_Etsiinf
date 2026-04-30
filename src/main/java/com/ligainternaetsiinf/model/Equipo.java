package com.ligainternaetsiinf.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "equipo")
    private List<Jugador> jugadores;

    private int puntos;
    private int golesAFavor;
    private int golesEnContra;
    private int diferenciaDeGoles;

    public Equipo(){}

    public Equipo(String name, List<Jugador> jugadores){
        this.name = name;
        this.jugadores = jugadores;
        this.puntos = 0;
        this.golesAFavor = 0;
        this.golesEnContra = 0;
        this.diferenciaDeGoles = 0;
    }

    public Integer getId(){ return id; }

    public String getName(){ return name; }

    public void setName(String name){ this.name = name; }

    public List<Jugador> getJugadores(){ return jugadores; }

    public void setJugadores(List<Jugador> jugadores){ this.jugadores = jugadores; }

    public int getPuntos(){ return puntos; }

    public void setPuntos(int puntos){ this.puntos = puntos; }

    public int getGolesAFavor(){ return golesAFavor; }

    public void setGolesAFavor(int golesAFavor){ this.golesAFavor = golesAFavor; }

    public int getGolesEnContra(){ return golesEnContra; }

    public void setGolesEnContra(int golesEnContra){ this.golesEnContra = golesEnContra; }

    public int getDiferenciaDeGoles(){ return diferenciaDeGoles; }

    public void setDiferenciaDeGoles(int diferenciaDeGoles){
        this.diferenciaDeGoles = diferenciaDeGoles;
    }
}









