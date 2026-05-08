package com.ligainternaetsiinf.dto;

import java.util.List;

public class AlineacionDTO {
    private List<Integer> jugadorFantasyIds; // IDs de los 7 jugadores alineados
    private String formacion;

    public AlineacionDTO(){}

    public List<Integer> getJugadorFantasyIds(){ return jugadorFantasyIds; }
    public void setJugadorFantasyIds(List<Integer> jugadorFantasyIds){ this.jugadorFantasyIds = jugadorFantasyIds; }
    public String getFormacion(){ return formacion; }
    public void setFormacion(String formacion){ this.formacion = formacion; }
}
