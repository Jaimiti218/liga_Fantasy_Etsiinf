package com.ligainternaetsiinf.dto;

public class UserResponse {
    private Integer id;
    private String username;
    private String role;
    private String fotoPerfil;

    public UserResponse(){}

    public UserResponse(Integer id, String username, String role, String fotoPerfil){
        this.id = id;
        this.username = username;
        this.role = role;
        this.fotoPerfil = fotoPerfil; 
    }

    public Integer getId(){ return id; }
    public String getUsername(){ return username; }
    public String getRole(){ return role; }
    public String getFotoPerfil(){ return fotoPerfil; } 
}
