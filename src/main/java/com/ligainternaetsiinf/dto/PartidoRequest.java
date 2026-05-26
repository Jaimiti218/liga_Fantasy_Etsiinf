package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;

public class PartidoRequest {
    private Integer equipoLocalId;
    private Integer equipoVisitanteId;
    private LocalDateTime fecha;
    private Integer jornada;

    public PartidoRequest(){}

    public Integer getEquipoLocalId(){ return equipoLocalId; }
    public void setEquipoLocalId(Integer equipoLocalId){ this.equipoLocalId = equipoLocalId; }
    public Integer getEquipoVisitanteId(){ return equipoVisitanteId; }
    public void setEquipoVisitanteId(Integer equipoVisitanteId){ this.equipoVisitanteId = equipoVisitanteId; }
    public LocalDateTime getFecha(){ return fecha; }
    public void setFecha(LocalDateTime fecha){ this.fecha = fecha; }
    public Integer getJornada(){ return jornada; }
    public void setJornada(Integer jornada){ this.jornada = jornada; }
}