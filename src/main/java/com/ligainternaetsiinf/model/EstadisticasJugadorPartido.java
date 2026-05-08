package com.ligainternaetsiinf.model;

import jakarta.persistence.*;

@Entity
public class EstadisticasJugadorPartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Jugador jugador;

    @ManyToOne
    private Partido partido;

    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;
    private boolean jugo; // si el jugador jugó o no ese partido
    private int puntosObtenidos; // puntos fantasy que obtuvo en este partido

    public EstadisticasJugadorPartido(){}

    public EstadisticasJugadorPartido(Jugador jugador, Partido partido){
        this.jugador = jugador;
        this.partido = partido;
        this.goles = 0;
        this.asistencias = 0;
        this.tarjetasAmarillas = 0;
        this.tarjetasRojas = 0;
        this.paradas = 0;
        this.jugo = false;
        this.puntosObtenidos = 0;
    }

    public Integer getId(){ return id; }
    public Jugador getJugador(){ return jugador; }
    public void setJugador(Jugador jugador){ this.jugador = jugador; }
    public Partido getPartido(){ return partido; }
    public void setPartido(Partido partido){ this.partido = partido; }
    public int getGoles(){ return goles; }
    public void setGoles(int goles){ this.goles = goles; }
    public int getAsistencias(){ return asistencias; }
    public void setAsistencias(int asistencias){ this.asistencias = asistencias; }
    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public void setTarjetasAmarillas(int tarjetasAmarillas){ this.tarjetasAmarillas = tarjetasAmarillas; }
    public int getTarjetasRojas(){ return tarjetasRojas; }
    public void setTarjetasRojas(int tarjetasRojas){ this.tarjetasRojas = tarjetasRojas; }
    public int getParadas(){ return paradas; }
    public void setParadas(int paradas){ this.paradas = paradas; }
    public boolean isJugo(){ return jugo; }
    public void setJugo(boolean jugo){ this.jugo = jugo; }
    public int getPuntosObtenidos(){ return puntosObtenidos; }
    public void setPuntosObtenidos(int puntosObtenidos){ this.puntosObtenidos = puntosObtenidos; }
}