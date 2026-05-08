package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;
import java.util.List;

public class JugadorFantasyDetalleResponse {
    private Integer id;
    private Integer jugadorRealId;
    private String nombre;
    private String posicion;
    private long valorMercado;
    private String nombreEquipoReal;
    private int puntosTotal;
    private double mediaPuntos;
    private int partidosJugados;
    private Long clausula;
    private LocalDateTime clausulaBloqueadaHasta;
    private boolean alineado;

    public static final String PORTERO   = "PORTERO";
    public static final String DEFENSA  = "DEFENSA";
    public static final String MEDIOCENTRO   = "MEDIOCENTRO";
    public static final String DELANTERO   = "DELANTERO";

    public JugadorFantasyDetalleResponse(){}

    public JugadorFantasyDetalleResponse(Integer id, Integer jugadorRealId, String nombre,
            String posicion, long valorMercado, String nombreEquipoReal,
            int puntosTotal, double mediaPuntos, int partidosJugados,
            Long clausula, LocalDateTime clausulaBloqueadaHasta, boolean alineado){
        this.id = id;
        this.jugadorRealId = jugadorRealId;
        this.nombre = nombre;
        this.valorMercado = valorMercado;
        this.nombreEquipoReal = nombreEquipoReal;
        this.puntosTotal = puntosTotal;
        this.mediaPuntos = mediaPuntos;
        this.partidosJugados = partidosJugados;
        this.clausula = clausula;
        this.clausulaBloqueadaHasta = clausulaBloqueadaHasta;
        this.alineado = alineado;

        if (!List.of(PORTERO, DEFENSA, MEDIOCENTRO, DELANTERO).contains(posicion)) {
            throw new IllegalArgumentException("La posicion de un jugador tiene que ser PORTERO, DEFENSA, MEDIOCENTRO O DELANTERO");
        }
        else
            this.posicion = posicion;
    }

    public Integer getId(){ return id; }
    public Integer getJugadorRealId(){ return jugadorRealId; }
    public String getNombre(){ return nombre; }
    public String getPosicion(){ return posicion; }
    public long getValorMercado(){ return valorMercado; }
    public String getNombreEquipoReal(){ return nombreEquipoReal; }
    public int getPuntosTotal(){ return puntosTotal; }
    public double getMediaPuntos(){ return mediaPuntos; }
    public int getPartidosJugados(){ return partidosJugados; }
    public Long getClausula(){ return clausula; }
    public LocalDateTime getClausulaBloqueadaHasta(){ return clausulaBloqueadaHasta; }
    public boolean isAlineado(){ return alineado; }
}
