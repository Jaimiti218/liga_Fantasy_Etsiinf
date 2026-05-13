package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;

public class PartidoResponse {
    private Integer id;
    private Integer equipoLocalId;
    private String equipoLocalNombre;
    private Integer equipoVisitanteId;
    private String equipoVisitanteNombre;
    private LocalDateTime fecha;
    private Integer jornada;
    private Integer golesLocal;
    private Integer golesVisitante;
    private boolean jugado;

    public PartidoResponse(){}

    public PartidoResponse(Integer id, Integer equipoLocalId, String equipoLocalNombre,
            Integer equipoVisitanteId, String equipoVisitanteNombre,
            LocalDateTime fecha, Integer jornada,
            Integer golesLocal, Integer golesVisitante, boolean jugado){
        this.id = id;
        this.equipoLocalId = equipoLocalId;
        this.equipoLocalNombre = equipoLocalNombre;
        this.equipoVisitanteId = equipoVisitanteId;
        this.equipoVisitanteNombre = equipoVisitanteNombre;
        this.fecha = fecha;
        this.jornada = jornada;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.jugado = jugado;
    }

    public Integer getId(){ return id; }
    public Integer getEquipoLocalId(){ return equipoLocalId; }
    public String getEquipoLocalNombre(){ return equipoLocalNombre; }
    public Integer getEquipoVisitanteId(){ return equipoVisitanteId; }
    public String getEquipoVisitanteNombre(){ return equipoVisitanteNombre; }
    public LocalDateTime getFecha(){ return fecha; }
    public Integer getJornada(){ return jornada; }
    public Integer getGolesLocal(){ return golesLocal; }
    public Integer getGolesVisitante(){ return golesVisitante; }
    public boolean isJugado(){ return jugado; }
}