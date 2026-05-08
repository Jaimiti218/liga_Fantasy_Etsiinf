package com.ligainternaetsiinf.dto;

import java.util.List;

public class JugadorUpdateDTO {/*mas adelante se pueden añadir aqui los demas atributos (como en el equipo) o no, depende como vea */

    private String fullName;
    private String posicion;
    private String nombreEquipo;



    public static final String PORTERO   = "PORTERO";
    public static final String DEFENSA  = "DEFENSA";
    public static final String MEDIOCENTRO   = "MEDIOCENTRO";
    public static final String DELANTERO   = "DELANTERO";

    public JugadorUpdateDTO(){}

    public JugadorUpdateDTO(String fullName,String posicion, String nombreEquipo){
        this.fullName = fullName;
        this.posicion = posicion;
        this.nombreEquipo = nombreEquipo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        if (!List.of(PORTERO, DEFENSA, MEDIOCENTRO, DELANTERO).contains(posicion)) {
            throw new IllegalArgumentException("La posicion de un jugador tiene que ser PORTERO, DEFENSA, MEDIOCENTRO O DELANTERO");
        }
        this.posicion = posicion;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }
}
