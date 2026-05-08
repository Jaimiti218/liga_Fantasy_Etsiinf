package com.ligainternaetsiinf.model;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public static final String PORTERO   = "PORTERO";
    public static final String DEFENSA  = "DEFENSA";
    public static final String MEDIOCENTRO   = "MEDIOCENTRO";
    public static final String DELANTERO   = "DELANTERO";


    private String fullName;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    private String posicion;
    private int partidosJugados;
    private long valorMercado;
    private int puntosFantasy;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int paradas;

    public Jugador(){}

    public Jugador(String fullName, Equipo equipo, String posicion){
        this.fullName = fullName;
        this.equipo = equipo;
        this.valorMercado = 10000000;
        this.puntosFantasy = 0;
        this.goles = 0;
        this.asistencias = 0;
        this.tarjetasAmarillas = 0;
        this.tarjetasRojas = 0;
        this.paradas = 0;
        this.partidosJugados = 0;
        if (!List.of(PORTERO, DEFENSA, MEDIOCENTRO, DELANTERO).contains(posicion)) {
            throw new IllegalArgumentException("La posicion de un jugador tiene que ser PORTERO, DEFENSA, MEDIOCENTRO O DELANTERO");
        }
        else
            this.posicion = posicion;
    }

    public Integer getId(){ return id; }

    public String getFullName(){ return fullName; }
    public void setFullName(String fullName){ this.fullName = fullName; }

    public Equipo getEquipo(){ return equipo; }
    public void setEquipo(Equipo equipo){ this.equipo = equipo; }

    public long getValorMercado(){ return valorMercado; }
    public void setValorMercado(long valorMercado){ this.valorMercado = valorMercado; }

    public int getPuntosFantasy(){ return puntosFantasy; }
    public void setPuntosFantasy(int puntosFantasy){ this.puntosFantasy = puntosFantasy; }

    public int getPartidosJugados(){ return partidosJugados; }
    public void setPartidosJugados(int partidosJugados){ this.partidosJugados = partidosJugados; }

    public int getGoles(){ return goles; }
    public void setGoles(int goles){ this.goles = goles; }

    public int getAsistencias(){ return asistencias; }
    public void setAsistencias(int asistencias){ this.asistencias = asistencias; }

    public int getTarjetasAmarillas(){ return tarjetasAmarillas; }
    public void setTarjetasAmarillas(int tarjetasAmarillas){ this.tarjetasAmarillas = tarjetasAmarillas; }

    public int getTarjetasRojas(){ return tarjetasRojas; }
    public void setTarjetasRojas(int tarjetasRojas){ this.tarjetasRojas = tarjetasRojas; }

    public int getParadas(){ return paradas; }
    public void setParadas(int paradas){ this.paradas = paradas; }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        if (!List.of(PORTERO, DEFENSA, MEDIOCENTRO, DELANTERO).contains(posicion)) {
            throw new IllegalArgumentException("La posicion de un jugador tiene que ser PORTERO, DEFENSA, MEDIOCENTRO O DELANTERO");
        }
        this.posicion = posicion;
    }
}