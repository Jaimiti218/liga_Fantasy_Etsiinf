package com.ligainternaetsiinf.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VentaResponse {
    private Integer jugadorFantasyId;
    private String jugadorNombre;
    private String posicion;
    private long valorMercado;
    private String equipoReal;
    private boolean enVenta;
    private List<OfertaRecibidaResponse> ofertas;
    private Long clausula;
    private LocalDateTime clausulaBloqueadaHasta;

    public VentaResponse(){}

    public VentaResponse(Integer jugadorFantasyId, String jugadorNombre, String posicion,
            long valorMercado, String equipoReal, boolean enVenta,
            List<OfertaRecibidaResponse> ofertas, Long clausula, LocalDateTime clausulaBloqueadaHasta){
        this.jugadorFantasyId = jugadorFantasyId;
        this.jugadorNombre    = jugadorNombre;
        this.posicion         = posicion;
        this.valorMercado     = valorMercado;
        this.equipoReal       = equipoReal;
        this.enVenta          = enVenta;
        this.ofertas          = ofertas;
        this.clausula = clausula;
        this.clausulaBloqueadaHasta = clausulaBloqueadaHasta;
    }

    public Integer getJugadorFantasyId(){ return jugadorFantasyId; }
    public String getJugadorNombre(){ return jugadorNombre; }
    public String getPosicion(){ return posicion; }
    public long getValorMercado(){ return valorMercado; }
    public String getEquipoReal(){ return equipoReal; }
    public boolean isEnVenta(){ return enVenta; }
    public List<OfertaRecibidaResponse> getOfertas(){ return ofertas; }
    public Long getClausula(){ return clausula; }
    public LocalDateTime getClausulaBloqueadaHasta(){ return clausulaBloqueadaHasta; }

    // DTO anidado para cada oferta recibida
    public static class OfertaRecibidaResponse {
        private Integer pujaId;
        private long cantidad;
        private String ofertante; // username o "La Liga"
        private LocalDateTime fecha;

        public OfertaRecibidaResponse(){}

        public OfertaRecibidaResponse(Integer pujaId, long cantidad,
                String ofertante, LocalDateTime fecha){
            this.pujaId    = pujaId;
            this.cantidad  = cantidad;
            this.ofertante = ofertante;
            this.fecha     = fecha;
        }

        public Integer getPujaId(){ return pujaId; }
        public long getCantidad(){ return cantidad; }
        public String getOfertante(){ return ofertante; }
        public LocalDateTime getFecha(){ return fecha; }
    }
}
