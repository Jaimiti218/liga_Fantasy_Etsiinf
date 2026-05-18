package com.ligainternaetsiinf.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;

@Entity
public class NoticiaFantasy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private LigaFantasy ligaFantasy;

    private String titulo;

    @Column(length = 2000)
    private String noticia;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;

    // null = noticia pública para todos en la liga
    // con valor = solo la ve ese usuario
    private Integer userIdDestinatario;

    public NoticiaFantasy(){}

    // Constructor para noticias públicas
    public NoticiaFantasy(LigaFantasy liga, String titulo, String noticia){
        this.ligaFantasy = liga;
        this.titulo = titulo;
        this.noticia = noticia;
        this.fecha = LocalDate.now();
        this.hora  = LocalTime.now();
        this.userIdDestinatario = null;
    }

    // Constructor para noticias privadas
    public NoticiaFantasy(LigaFantasy liga, String titulo, String noticia, Integer userId){
        this.ligaFantasy = liga;
        this.titulo = titulo;
        this.noticia = noticia;
        this.fecha = LocalDate.now();
        this.hora  = LocalTime.now();
        this.userIdDestinatario = userId;
    }

    public Integer getId(){ return id; }
    public LigaFantasy getLigaFantasy(){ return ligaFantasy; }
    public String getTitulo(){ return titulo; }
    public String getNoticia(){ return noticia; }
    public LocalDate getFecha(){ return fecha; }
    public LocalTime getHora(){ return hora; }
    public Integer getUserIdDestinatario(){ return userIdDestinatario; }
}