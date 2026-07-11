package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.AlineacionEquipoJornada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlineacionEquipoJornadaRepository extends JpaRepository<AlineacionEquipoJornada, Integer> {

    Optional<AlineacionEquipoJornada> findByEquipoFantasyIdAndJornada(Integer equipoId, Integer jornada);

    Optional<List<AlineacionEquipoJornada>> findByEquipoFantasyId(Integer equipoId);

    List<AlineacionEquipoJornada> findByJornada(Integer jornada);

    @Query("SELECT a FROM AlineacionEquipoJornada a JOIN a.jugadoresAlineados j WHERE j.id = :jugadorId")
    List<AlineacionEquipoJornada> findByJugadorAlineadoId(@Param("jugadorId") Integer jugadorId);
}
