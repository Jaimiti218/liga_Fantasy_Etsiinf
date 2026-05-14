package com.ligainternaetsiinf.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String password;
    private String username;

    private String role; //esto podra ser "USER" para la gente normal o "ADMIN" para los administradores

    @Column(columnDefinition = "LONGTEXT")
    private String fotoPerfil; // base64 o null
    @OneToMany
    private List<LigaFantasy> ligasFantasy;

    public User(){}

    public User(String email, String password, String username){
        this.email = email;
        this.password = password;
        this.username = username;
        this.ligasFantasy = new ArrayList<>();
        this.role = "USER"; //por defecto lo vamos a crear como usuario normal, y si queremos que sea admin, otro administrador tendra que cambiarle el rol a mano
    }

    public Integer getId(){ return id; }

    public String getEmail(){ return email; }

    public String getPassword(){ return password; }
    public String getRole(){    return role;  }

    public void setRole(String role){    this.role = role;  }

    public void setEmail(String email){ this.email = email; }

    public String getUsername(){ return username; }

    public void setUsername(String username){ this.username = username; }

    public List<LigaFantasy> getLigasFantasy(){  return ligasFantasy;  }

    public String getFotoPerfil(){ return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil){ this.fotoPerfil = fotoPerfil; }

}