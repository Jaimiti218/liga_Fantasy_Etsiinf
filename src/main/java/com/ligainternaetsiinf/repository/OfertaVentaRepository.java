package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.OfertaVenta;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.JugadorFantasy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfertaVentaRepository extends JpaRepository<OfertaVenta, Integer> {
    List<OfertaVenta> findByEquipoVendedorAndAceptadaFalse(EquipoFantasy equipo);
    List<OfertaVenta> findByJugadorFantasyAndAceptadaFalse(JugadorFantasy jugador);
}
