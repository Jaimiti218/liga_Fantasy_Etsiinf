package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.EstadisticasJugadorPartido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstadisticasJugadorPartidoRepository extends JpaRepository<EstadisticasJugadorPartido, Integer> {

    List<EstadisticasJugadorPartido> findByJugadorId(Integer jugadorId);

    List<EstadisticasJugadorPartido> findByPartidoId(Integer partidoId);

    Optional<EstadisticasJugadorPartido> findByJugadorIdAndPartidoId(Integer jugadorId, Integer partidoId);

    List<EstadisticasJugadorPartido> findByPartidoJornada(Integer jornada);
}
