package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class Puja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private EquipoFantasy equipoComprador; // null si es oferta del sistema

    @ManyToOne
    private EquipoFantasy equipoVendedor; // el que tiene el jugador

    @ManyToOne
    private JugadorFantasy jugadorFantasy;

    @ManyToOne
    private InstanciaMercado instancia; // null si es oferta directa de otro usuario, sea o no a traves del mercado

    private long cantidad;
    private LocalDateTime fecha;
    private boolean aceptada;  // true si se aceptó esta puja
    private boolean resuelta;  // true si ya no está pendiente (aceptada o rechazada)

    private boolean esClausulazo = false;
    private Long valorClausulaMomento = null; // valor de la clausula cuando se ejecutó

    public Puja(){}

    // Constructor para puja en mercado normal
    public Puja(EquipoFantasy comprador, EquipoFantasy vendedor,
            JugadorFantasy jugador, InstanciaMercado instancia, long cantidad){
        this.equipoComprador = comprador;
        this.equipoVendedor  = vendedor;
        this.jugadorFantasy  = jugador;
        this.instancia       = instancia;
        this.cantidad        = cantidad;
        this.fecha           = LocalDateTime.now();
        this.aceptada        = false;
        this.resuelta        = false;
    }

    // Constructor para oferta directa (sin instancia de mercado)
    public Puja(EquipoFantasy comprador, EquipoFantasy vendedor,
            JugadorFantasy jugador, long cantidad){
        this.equipoComprador = comprador;
        this.equipoVendedor  = vendedor;
        this.jugadorFantasy  = jugador;
        this.instancia       = null;
        this.cantidad        = cantidad;
        this.fecha           = LocalDateTime.now();
        this.aceptada        = false;
        this.resuelta        = false;
    }

    // Constructor para oferta del sistema (comprador null)
    public Puja(EquipoFantasy vendedor, JugadorFantasy jugador, long cantidad){
        this.equipoComprador = null;
        this.equipoVendedor  = vendedor;
        this.jugadorFantasy  = jugador;
        this.instancia       = null;
        this.cantidad        = cantidad;
        this.fecha           = LocalDateTime.now();
        this.aceptada        = false;
        this.resuelta        = false;
    }

    public Integer getId(){ return id; }
    public EquipoFantasy getEquipoComprador(){ return equipoComprador; }
    public EquipoFantasy getEquipoVendedor(){ return equipoVendedor; }
    public JugadorFantasy getJugadorFantasy(){ return jugadorFantasy; }
    public InstanciaMercado getInstancia(){ return instancia; }
    public long getCantidad(){ return cantidad; }
    public void setCantidad(long cantidad){ this.cantidad = cantidad; }
    public LocalDateTime getFecha(){ return fecha; }
    public boolean isAceptada(){ return aceptada; }
    public void setAceptada(boolean aceptada){ this.aceptada = aceptada; }
    public boolean isResuelta(){ return resuelta; }
    public void setResuelta(boolean resuelta){ this.resuelta = resuelta; }
    public boolean isEsClausulazo(){ return esClausulazo; }
    public void setEsClausulazo(boolean esClausulazo){ this.esClausulazo = esClausulazo; }
    public Long getValorClausulaMomento(){ return valorClausulaMomento; }
    public void setValorClausulaMomento(Long v){ this.valorClausulaMomento = v; }
}