package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.AlineacionEquipoJornada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlineacionEquipoJornadaRepository extends JpaRepository<AlineacionEquipoJornada, Integer> {

    Optional<AlineacionEquipoJornada> findByEquipoFantasyIdAndJornada(Integer equipoId, Integer jornada);

    List<AlineacionEquipoJornada> findByJornada(Integer jornada);
}
