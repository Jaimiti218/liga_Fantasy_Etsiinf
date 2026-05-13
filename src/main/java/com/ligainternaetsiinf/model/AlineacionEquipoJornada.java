package com.ligainternaetsiinf.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class AlineacionEquipoJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private EquipoFantasy equipoFantasy;

    @ManyToMany
    private List<JugadorFantasy> jugadoresAlineados;

    private Integer jornada;
    private String formacion;
    private LocalDateTime fechaRegistro;

    public AlineacionEquipoJornada(){}

    public AlineacionEquipoJornada(EquipoFantasy equipo, List<JugadorFantasy> jugadoresAlineados,
            Integer jornada, String formacion){
        this.equipoFantasy  = equipo;
        this.jugadoresAlineados = jugadoresAlineados;
        this.jornada        = jornada;
        this.formacion      = formacion;
        this.fechaRegistro  = LocalDateTime.now();
    }

    public Integer getId(){ return id; }
    public EquipoFantasy getEquipoFantasy(){ return equipoFantasy; }
    public List<JugadorFantasy> getJugadoresAlineados(){ return jugadoresAlineados; }
    public Integer getJornada(){ return jornada; }
    public String getFormacion(){ return formacion; }
    public LocalDateTime getFechaRegistro(){ return fechaRegistro; }
}