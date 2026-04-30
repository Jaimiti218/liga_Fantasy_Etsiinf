package com.ligainternaetsiinf.dto;

public class JugadorUpdateDTO {/*mas adelante se pueden añadir aqui los demas atributos (como en el equipo) o no, depende como vea */

    private String fullName;
    private Boolean esPortero;
    private String nombreEquipo;

    public JugadorUpdateDTO(){}

    public JugadorUpdateDTO(String fullName, Boolean esPortero, String nombreEquipo){
        this.fullName = fullName;
        this.esPortero = esPortero;
        this.nombreEquipo = nombreEquipo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getEsPortero() {
        return esPortero;
    }

    public void setEsPortero(Boolean esPortero) {
        this.esPortero = esPortero;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }
}
