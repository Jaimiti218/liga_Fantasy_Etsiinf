package com.ligainternaetsiinf.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
public class Transfer { //esta entidad se va a usar para calcular los valores
                        //de mercado de los jugadores, y quizas alguna otra cosa tambien

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private JugadorFantasy jugador;

    private int precioTraspaso;

    private String tipo; //compra (al mercado), venta, clausulazo (tambien es compra)

    private LocalDate fecha;

    public Transfer(){}

    public Transfer(JugadorFantasy jugador, int precioTraspaso, String tipo){
        this.jugador = jugador;
        this.precioTraspaso = precioTraspaso;
        this.tipo = tipo;
        this.fecha = LocalDate.now();
    }

    
}

