package com.ligainternaetsiinf.dto;

/* Resumen ligero de una liga del usuario para la pantalla "Mis ligas":
   se obtiene con consultas escalares (sin cargar entidades completas). */
public class MiLigaResumenResponse {
    private Integer id;
    private String name;
    private String code;
    private Integer equipoId;
    private int puntos;
    private long dinero;
    private int posicion;
    private boolean tieneAviso;

    public MiLigaResumenResponse(Integer id, String name, String code, Integer equipoId,
            int puntos, long dinero, int posicion, boolean tieneAviso) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.equipoId = equipoId;
        this.puntos = puntos;
        this.dinero = dinero;
        this.posicion = posicion;
        this.tieneAviso = tieneAviso;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public Integer getEquipoId() { return equipoId; }
    public int getPuntos() { return puntos; }
    public long getDinero() { return dinero; }
    public int getPosicion() { return posicion; }
    public boolean isTieneAviso() { return tieneAviso; }
}
