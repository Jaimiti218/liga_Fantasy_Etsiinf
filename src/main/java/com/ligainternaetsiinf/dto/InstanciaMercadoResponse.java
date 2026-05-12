package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InstanciaMercadoResponse {
    private Integer id;
    private LocalDateTime fin;
    private List<JugadorFantasyDetalleResponse> jugadores;

    public InstanciaMercadoResponse(){}

    public InstanciaMercadoResponse(Integer id, LocalDateTime fin, List<JugadorFantasyDetalleResponse> jugadores){
        this.id = id;
        this.fin = fin;
        this.jugadores = jugadores;
    }

    public Integer getId(){ return id; }
    public LocalDateTime getFin(){ return fin; }
    public List<JugadorFantasyDetalleResponse> getJugadores(){ return jugadores; }
}
