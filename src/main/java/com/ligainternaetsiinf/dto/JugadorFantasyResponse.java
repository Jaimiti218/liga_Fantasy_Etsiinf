package com.ligainternaetsiinf.dto;

import java.util.List;

public class JugadorFantasyResponse {
    private Integer id;

    private Integer jugadorRealId;
    private String jugadorRealNombre;
    private String posicion;
    private long valorMercado;

    private Integer ligaId;
    private String ligaNombre;

    private Integer equipoId;
    private String duenoEquipoFantasyNombreCompleto;

    private Long clausula;


    public static final String PORTERO   = "PORTERO";
    public static final String DEFENSA  = "DEFENSA";
    public static final String MEDIOCENTRO   = "MEDIOCENTRO";
    public static final String DELANTERO   = "DELANTERO";

    public JugadorFantasyResponse(){}

    public JugadorFantasyResponse(Integer id, Integer jugadorRealId, String jugadorRealNombre, String posicion,
     long valorMercado, Integer ligaId, String ligaNombre, Integer equipoId, String duenoEquipoFantasyNombreCompleto, Long clausula){
        this.id = id;
        this.jugadorRealId = jugadorRealId;
        this.jugadorRealNombre = jugadorRealNombre;
        
        this.valorMercado = valorMercado;
        this.ligaId = ligaId;
        this.ligaNombre = ligaNombre;
        this.equipoId = equipoId;
        this.duenoEquipoFantasyNombreCompleto = duenoEquipoFantasyNombreCompleto;
        this.clausula = clausula;

        if (!List.of(PORTERO, DEFENSA, MEDIOCENTRO, DELANTERO).contains(posicion)) {
            throw new IllegalArgumentException("La posicion de un jugador tiene que ser PORTERO, DEFENSA, MEDIOCENTRO O DELANTERO");
        }
        else
            this.posicion = posicion;
    }

    public Integer getId(){ return id; }

    public Integer getJugadorRealId(){ return jugadorRealId; }

    public String getJugadorRealNombre(){ return jugadorRealNombre; }
    
    public String getPosicion(){ return posicion; }
    
    public long getValorMercado(){ return valorMercado; }

    public Integer getLigaId(){ return ligaId; }

    public String getLigaNombre(){ return ligaNombre; }

    public Integer getEquipoId(){ return equipoId; }

    public String getDuenoEquipoFantasyNombreCompleto(){ return duenoEquipoFantasyNombreCompleto; }

    public Long getClausula(){ return clausula; }
}
