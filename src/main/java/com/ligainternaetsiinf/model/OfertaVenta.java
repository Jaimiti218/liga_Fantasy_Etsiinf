package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class OfertaVenta {  /*cuando termine una instancia de mercado habra que comprobar todas las OfertaVenta
    que se habian hecho en esa instancia, y si el atributo enVenta del jugadoFantasy es true y aceptada aqui es
    false, creas una nueva ofertaVenta para el mismo jugador pero con una cantidad distinta */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private JugadorFantasy jugadorFantasy;

    @ManyToOne
    private EquipoFantasy equipoVendedor;

    @ManyToOne
    private EquipoFantasy equipoComprador;

    private long cantidad;
    private LocalDateTime fecha;
    private boolean aceptada;

    public OfertaVenta(){}

    public OfertaVenta(JugadorFantasy jugador, EquipoFantasy vendedor,  EquipoFantasy comprador, long cantidad){
        this.jugadorFantasy = jugador;
        this.equipoVendedor = vendedor;
        this.equipoComprador = comprador;
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now();
        this.aceptada = false;
    }

    public Integer getId(){ return id; }
    public JugadorFantasy getJugadorFantasy(){ return jugadorFantasy; }
    public EquipoFantasy getEquipoVendedor(){ return equipoVendedor; }
    public long getCantidad(){ return cantidad; }
    public boolean isAceptada(){ return aceptada; }
    public void setAceptada(boolean aceptada){ this.aceptada = aceptada; }
    public LocalDateTime getFecha(){ return fecha; }
    public EquipoFantasy getEquipoComprador(){ return equipoComprador; }
}
