package com.ligainternaetsiinf.dto;

import java.util.List;

public class EquipoUpdateDTO { /*esta clase la he creado ya que el frontend, cuando no le introduces ningún dato en un atributo, te lo pone por defecto a null
    0, o false, depende del tipo de dato, entonces por ejemplo para los int, no habia forma de comprobar si cuando el los goles a favor, por ejemplo,
    es porque no se ha introducido nada o porque es un 0, por eso creamos esta clase, para hacerlo un integer en vez de int, que ahi si se puede comprobar */

    private String nombre;

    private List<String> jugadores; // nombres de jugadores

    private Integer puntos;

    private Integer golesAFavor;

    private Integer golesEnContra;

    private Integer diferenciaDeGoles;

    public EquipoUpdateDTO(){}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<String> jugadores) {
        this.jugadores = jugadores;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public Integer getGolesAFavor() {
        return golesAFavor;
    }

    public void setGolesAFavor(Integer golesAFavor) {
        this.golesAFavor = golesAFavor;
    }

    public Integer getGolesEnContra() {
        return golesEnContra;
    }

    public void setGolesEnContra(Integer golesEnContra) {
        this.golesEnContra = golesEnContra;
    }

    public Integer getDiferenciaDeGoles() {
        return diferenciaDeGoles;
    }

    public void setDiferenciaDeGoles(Integer diferenciaDeGoles) {
        this.diferenciaDeGoles = diferenciaDeGoles;
    }
}
