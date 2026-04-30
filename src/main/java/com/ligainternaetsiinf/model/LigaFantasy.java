package com.ligainternaetsiinf.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class LigaFantasy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(unique = true)
    private String code; /*este es el codigo para que tus amigos se unan a la liga */

    @ManyToOne
    private User creator;

    @OneToMany(mappedBy = "ligaFantasy")
    private List<EquipoFantasy> equipos;

    @OneToOne(mappedBy = "ligaFantasy")
    private Mercado mercado;

    private int maxPlayers;

    private LocalDate createdAt;

    public LigaFantasy(){}

    public LigaFantasy(String name, String code, User creator){
        this.name = name;
        this.code = code;
        this.creator = creator;
        this.maxPlayers = 20;
        this.createdAt = LocalDate.now();
    }

    public Integer getId(){ return id; }

    public String getName(){ return name; }

    public void setName(String name){ this.name = name; }

    public String getCode(){ return code; }

    public void setCode(String code){ this.code = code; }

    public User getCreator(){ return creator; }

    public void setCreator(User creator){ this.creator = creator; }

    public int getMaxPlayers(){ return maxPlayers; }

    public void setMaxPlayers(int maxPlayers){ this.maxPlayers = maxPlayers; }

    public LocalDate getCreatedAt(){ return createdAt; }

    public List<EquipoFantasy> getEquipos(){ return equipos; }


}