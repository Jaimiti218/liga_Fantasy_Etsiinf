package com.ligainternaetsiinf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.LigaFantasy;

import java.util.List;
import java.util.Optional;

public interface JugadorFantasyRepository extends JpaRepository<JugadorFantasy, Integer> {

    List<JugadorFantasy> findByLigaFantasyId(Integer ligaId);

    List<JugadorFantasy> findByLigaFantasyIdAndEquipoFantasyIsNull(Integer ligaId);

    List<JugadorFantasy> findByEquipoFantasyId(Integer equipoId);

    Optional<JugadorFantasy> findById(Integer id);

    List<JugadorFantasy> findByEquipoFantasyIdAndAlineadoTrue(Integer equipoId);

}
