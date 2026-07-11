package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstanciaMercadoRepository extends JpaRepository<InstanciaMercado, Integer> {
    Optional<InstanciaMercado> findByMercadoAndResueltaFalse(Mercado mercado);
    List<InstanciaMercado> findByResueltaFalse();

    @Query("SELECT i FROM InstanciaMercado i JOIN i.jugadoresDisponibles j WHERE j.id = :jugadorId")
    List<InstanciaMercado> findByJugadorDisponibleId(@Param("jugadorId") Integer jugadorId);
}