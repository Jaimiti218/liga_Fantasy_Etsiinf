package com.ligainternaetsiinf.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
public class HistorialPrecioJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    private long precio;
    private LocalDate fecha;

    public HistorialPrecioJugador(){}

    public HistorialPrecioJugador(Jugador jugador, long precio){
        this.jugador = jugador;
        this.precio  = precio;
        this.fecha   = LocalDate.now();
    }

    public Integer getId(){ return id; }
    public Jugador getJugador(){ return jugador; }
    public long getPrecio(){ return precio; }
    public LocalDate getFecha(){ return fecha; }
}