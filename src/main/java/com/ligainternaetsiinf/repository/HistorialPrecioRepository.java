package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.HistorialPrecioJugador;
import com.ligainternaetsiinf.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistorialPrecioRepository extends
        JpaRepository<HistorialPrecioJugador, Integer> {

    List<HistorialPrecioJugador> findByJugadorOrderByFechaDesc(Jugador jugador);

    Optional<HistorialPrecioJugador> findFirstByJugadorAndFechaLessThanEqualOrderByFechaDesc(
        Jugador jugador, LocalDate fecha);

        Optional<HistorialPrecioJugador> findFirstByJugadorAndFechaGreaterThanEqualOrderByFechaAsc(
    Jugador jugador, LocalDate fecha);

    // Para la pantalla de subidas/bajadas: todos los registros de una fecha concreta
    List<HistorialPrecioJugador> findByFecha(LocalDate fecha);

    void deleteByJugadorId(Integer jugadorId);
}