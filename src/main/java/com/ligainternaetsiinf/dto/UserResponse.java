package com.ligainternaetsiinf.dto;

public class UserResponse {
    private Integer id;
    private String username;

    public UserResponse(){
    }

    public UserResponse(Integer id, String username){
        this.id = id;
        this.username = username;
    }

    public Integer getId(){ return id; }
    public String getUsername(){ return username; }
}
