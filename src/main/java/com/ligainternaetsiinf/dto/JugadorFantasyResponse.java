package com.ligainternaetsiinf.dto;

public class JugadorFantasyResponse {
    private Integer id;

    private Integer jugadorRealId;
    private String jugadorRealNombre;
    private boolean esPortero;
    private long valorMercado;

    private Integer ligaId;
    private String ligaNombre;

    private Integer equipoId;
    private String duenoEquipoFantasyNombreCompleto;

    private Long clausula;

    public JugadorFantasyResponse(){}

    public JugadorFantasyResponse(Integer id, Integer jugadorRealId, String jugadorRealNombre, boolean esPortero,
     long valorMercado, Integer ligaId, String ligaNombre, Integer equipoId, String duenoEquipoFantasyNombreCompleto, Long clausula){
        this.id = id;
        this.jugadorRealId = jugadorRealId;
        this.jugadorRealNombre = jugadorRealNombre;
        this.esPortero = esPortero;
        this.valorMercado = valorMercado;
        this.ligaId = ligaId;
        this.ligaNombre = ligaNombre;
        this.equipoId = equipoId;
        this.duenoEquipoFantasyNombreCompleto = duenoEquipoFantasyNombreCompleto;
        this.clausula = clausula;
    }

    public Integer getId(){ return id; }

    public Integer getJugadorRealId(){ return jugadorRealId; }

    public String getJugadorRealNombre(){ return jugadorRealNombre; }
    
    public boolean getEsPortero(){ return esPortero; }
    
    public long getValorMercado(){ return valorMercado; }

    public Integer getLigaId(){ return ligaId; }

    public String getLigaNombre(){ return ligaNombre; }

    public Integer getEquipoId(){ return equipoId; }

    public String getDuenoEquipoFantasyNombreCompleto(){ return duenoEquipoFantasyNombreCompleto; }

    public Long getClausula(){ return clausula; }
}
