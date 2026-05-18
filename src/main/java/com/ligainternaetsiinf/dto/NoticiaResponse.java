package com.ligainternaetsiinf.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class NoticiaResponse {
    private Integer id;
    private String titulo;
    private String noticia;
    private LocalDate fecha;
    private LocalTime hora;
    private boolean esPrivada;

    public NoticiaResponse(){}

    public NoticiaResponse(Integer id, String titulo, String noticia,
            LocalDate fecha, LocalTime hora, boolean esPrivada){
        this.id = id;
        this.titulo = titulo;
        this.noticia = noticia;
        this.fecha = fecha;
        this.hora = hora;
        this.esPrivada = esPrivada;
    }

    public Integer getId(){ return id; }
    public String getTitulo(){ return titulo; }
    public String getNoticia(){ return noticia; }
    public LocalDate getFecha(){ return fecha; }
    public LocalTime getHora(){ return hora; }
    public boolean isEsPrivada(){ return esPrivada; }
}