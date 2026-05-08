package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {
    List<Partido> findByJornada(Integer jornada);
    List<Partido> findByJugadoFalseOrderByFechaAsc();
}
