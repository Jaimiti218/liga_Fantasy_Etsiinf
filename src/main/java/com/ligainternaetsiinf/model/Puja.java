package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class Puja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private EquipoFantasy equipoFantasy;

    @ManyToOne
    private JugadorFantasy jugadorFantasy;

    @ManyToOne
    private InstanciaMercado instancia;

    private long cantidad;
    private LocalDateTime fecha; //esto no lo veo del todo util pero bueno
    private boolean ganadora;

    public Puja(){}

    public Puja(EquipoFantasy equipo, JugadorFantasy jugador, InstanciaMercado instancia, long cantidad){
        this.equipoFantasy = equipo;
        this.jugadorFantasy = jugador;
        this.instancia = instancia;
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now();
        this.ganadora = false;
    }

    public Integer getId(){ return id; }
    public EquipoFantasy getEquipoFantasy(){ return equipoFantasy; }
    public JugadorFantasy getJugadorFantasy(){ return jugadorFantasy; }
    public InstanciaMercado getInstancia(){ return instancia; }
    public long getCantidad(){ return cantidad; }
    public void setCantidad(long cantidad){ this.cantidad = cantidad; }
    public LocalDateTime getFecha(){ return fecha; }
    public boolean isGanadora(){ return ganadora; }
    public void setGanadora(boolean ganadora){ this.ganadora = ganadora; }
}
