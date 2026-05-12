package com.ligainternaetsiinf.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class JugadorFantasy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Jugador jugadorReal; //el equipo real, y el precioFantasy (atributo valorMercado) del jugador lo sacas de aqui!!!

    @ManyToOne
    private LigaFantasy ligaFantasy;

    @ManyToOne
    private EquipoFantasy equipoFantasy;

    private Long clausula; /*es Long y no long por lo mismo que int e Integer, porque el valor de la clausula puede ser null si el jugador
    no pertenece aun a ningun equipo */

    private boolean alineado;

    private LocalDateTime clausulaBloqueadaHasta;

    private LocalDateTime fechaCompra;

    private boolean enVenta = false;

    public JugadorFantasy(){}

    public JugadorFantasy(Jugador jugadorReal, LigaFantasy ligaFantasy){
        this.jugadorReal = jugadorReal;
        this.ligaFantasy = ligaFantasy;
        this.alineado = false;
    }

    public Integer getId(){ return id; }

    public Jugador getJugadorReal(){ return jugadorReal; }

    public void setJugadorReal(Jugador jugadorReal){
        this.jugadorReal = jugadorReal;
    }

    public LigaFantasy getLigaFantasy(){
        return ligaFantasy;
    }

    public EquipoFantasy getEquipoFantasy(){
        return equipoFantasy;
    }

    public void setEquipoFantasy(EquipoFantasy equipoFantasy){
        this.equipoFantasy = equipoFantasy;
    }

    public Long getClausula(){
        return clausula;
    }

    public LocalDateTime getClausulaBloqueadaHasta(){
        return clausulaBloqueadaHasta;
    }

    public LocalDateTime getFechaCompra(){
        return fechaCompra;
    }

    public void setClausula(Long clausula){
        this.clausula = clausula;
    }

    public void setFechaCompra(LocalDateTime fecha){
        this.fechaCompra = fecha;
    }

    public void setClausulaBloqueadaHasta(LocalDateTime fecha){
        this.clausulaBloqueadaHasta = fecha;
    }

    public boolean isAlineado(){ return alineado; }
    public void setAlineado(boolean alineado){ this.alineado = alineado; }

    public boolean isEnVenta(){ return enVenta; }
    public void setEnVenta(boolean enVenta){ this.enVenta = enVenta; }

}
