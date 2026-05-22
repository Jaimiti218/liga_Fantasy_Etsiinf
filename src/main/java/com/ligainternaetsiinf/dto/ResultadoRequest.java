package com.ligainternaetsiinf.dto;

import java.util.List;

public class ResultadoRequest {
    private Integer golesLocal;
    private Integer golesVisitante;
    private List<EstadisticaJugadorRequest> estadisticas;

    public ResultadoRequest(){}

    public Integer getGolesLocal(){ return golesLocal; }
    public void setGolesLocal(Integer golesLocal){ this.golesLocal = golesLocal; }
    public Integer getGolesVisitante(){ return golesVisitante; }
    public void setGolesVisitante(Integer golesVisitante){ this.golesVisitante = golesVisitante; }
    public List<EstadisticaJugadorRequest> getEstadisticas(){ return estadisticas; }
    public void setEstadisticas(List<EstadisticaJugadorRequest> estadisticas){ this.estadisticas = estadisticas; }

    public static class EstadisticaJugadorRequest {
        private Integer jugadorId;
        private boolean jugo;
        private int goles;
        private int asistencias;
        private int tarjetasAmarillas;
        private int tarjetasRojas;
        private int paradas;

        public EstadisticaJugadorRequest(){}

        public Integer getJugadorId(){ return jugadorId; }
        public void setJugadorId(Integer jugadorId){ this.jugadorId = jugadorId; }
        public boolean isJugo(){ return jugo; }
        public void setJugo(boolean jugo){ this.jugo = jugo; }
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
    }
}