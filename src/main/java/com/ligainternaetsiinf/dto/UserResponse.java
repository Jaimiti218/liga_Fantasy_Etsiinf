package com.ligainternaetsiinf.dto;

public class UserResponse {
    private Integer id;
    private String username;
    private String role;

    public UserResponse(){
    }

    public UserResponse(Integer id, String username, String role){
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Integer getId(){ return id; }
    public String getUsername(){ return username; }
    public String getRole(){ return role; }
}
