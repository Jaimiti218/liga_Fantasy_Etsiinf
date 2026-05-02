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
    }

    public Integer getId(){ return id; }
    public String getUsername(){ return username; }
}
