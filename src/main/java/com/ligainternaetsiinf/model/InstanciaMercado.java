package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class InstanciaMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Mercado mercado;

    @ManyToMany
    private List<JugadorFantasy> jugadoresDisponibles;

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private boolean resuelta;

    public InstanciaMercado(){}

    public InstanciaMercado(Mercado mercado, List<JugadorFantasy> jugadores){
        this.mercado = mercado;
        this.jugadoresDisponibles = jugadores;
        this.inicio = LocalDateTime.now();
        this.fin = this.inicio.plusHours(24);
        this.resuelta = false;
    }

    public Integer getId(){ return id; }
    public Mercado getMercado(){ return mercado; }
    public List<JugadorFantasy> getJugadoresDisponibles(){ return jugadoresDisponibles; }
    public void setJugadoresDisponibles(List<JugadorFantasy> j){ this.jugadoresDisponibles = j; }
    public LocalDateTime getInicio(){ return inicio; }
    public LocalDateTime getFin(){ return fin; }
    public boolean isResuelta(){ return resuelta; }
    public void setResuelta(boolean resuelta){ this.resuelta = resuelta; }
}