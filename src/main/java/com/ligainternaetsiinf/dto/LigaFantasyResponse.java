package com.ligainternaetsiinf.dto;

public class LigaFantasyResponse { /*Esta clase la hemos creado ya que si no, al devolver un objeto del tipo LigaFantasy en el controller,
    se podia filtrar la contraseña del usuario creador de esa liga, ya que User creator es un atributo de LigFantasy */

    private Integer id;
    private String name;
    private String code;

    private Integer creatorId;
    private String creatorUsername;

    private Integer maxPlayers;

    public LigaFantasyResponse(){}

    public LigaFantasyResponse(Integer id, String name, String code, Integer creatorId, String creatorUsername, Integer maxPlayers){
        this.id = id;
        this.name = name;
        this.code = code;
        this.creatorId = creatorId;
        this.creatorUsername = creatorUsername;
        this.maxPlayers = maxPlayers;
    }

    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getCode(){ return code; }
    public void setCode(String code){ this.code = code; }

    public Integer getCreatorId(){ return creatorId; }
    public void setCreatorId(Integer creatorId){ this.creatorId = creatorId; }

    public String getCreatorUsername(){ return creatorUsername; }
    public void setCreatorUsername(String creatorUsername){ this.creatorUsername = creatorUsername; }

    public Integer getMaxPlayers(){ return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers){ this.maxPlayers = maxPlayers; }

}