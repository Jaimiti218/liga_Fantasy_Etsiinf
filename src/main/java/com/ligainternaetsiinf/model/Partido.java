package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Equipo equipoLocal;

    @ManyToOne
    private Equipo equipoVisitante;

    private LocalDateTime fecha;

    private Integer golesLocal;

    private Integer golesVisitante;

    private boolean jugado;

    private Integer jornada;

    public Partido() {
    }

    public Partido(Equipo equipoLocal, Equipo equipoVisitante, LocalDateTime fecha, Integer jornada) {
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.fecha = fecha;
        this.jugado = false;
        this.golesLocal = null;
        this.golesVisitante = null;
        this.jornada = jornada;
    }

    public Integer getId() {
        return id;
    }

    public Equipo getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(Equipo equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public Equipo getEquipoVisitante() {
        return equipoVisitante;
    }

    public void setEquipoVisitante(Equipo equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public boolean isJugado() {
        return jugado;
    }

    public void setJugado(boolean jugado) {
        this.jugado = jugado;
    }

    public Integer getJornada(){ return jornada; }
    public void setJornada(Integer jornada){ this.jornada = jornada; }
}