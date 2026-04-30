package com.ligainternaetsiinf.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class NoticiaFantasy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private LigaFantasy ligaFantasy;

    /* private String titulo; esto de momento lo comento porque no creo que haga falta, 
     la idea es, cuando ocurra algo, clausulazo, fichaje, lo que sea, se creará
     un objeto noticia, con el contenido en el string de noticia que sea*/

    @Column(length = 2000)
    private String noticia;

    private LocalDate fecha;

    public NoticiaFantasy(){}

    public NoticiaFantasy(String noticia){
        this.noticia = noticia;
        this.fecha = LocalDate.now();
    }

}
