package com.ligainternaetsiinf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ligainternaetsiinf.model.JugadorFantasy;


import java.util.List;
import java.util.Optional;

public interface JugadorFantasyRepository extends JpaRepository<JugadorFantasy, Integer> {

    List<JugadorFantasy> findByLigaFantasyId(Integer ligaId);

    List<JugadorFantasy> findByLigaFantasyIdAndEquipoFantasyIsNull(Integer ligaId);

    List<JugadorFantasy> findByEquipoFantasyId(Integer equipoId);

    List<JugadorFantasy> findByJugadorRealId(Integer jugadorRealId);

    Optional<JugadorFantasy> findById(Integer id);

    List<JugadorFantasy> findByEquipoFantasyIdAndAlineadoTrue(Integer equipoId);

    List<JugadorFantasy> findByLigaFantasyIdAndEnVentaTrue(Integer ligaId);

    List<JugadorFantasy> findByEquipoFantasyIdAndEnVentaTrue(Integer equipoId);

    @Query("SELECT jf FROM JugadorFantasy jf WHERE jf.ligaFantasy.id = :ligaId " +
        "AND jf.equipoFantasy IS NULL " +
        "AND jf NOT IN " +
        "(SELECT j FROM InstanciaMercado im JOIN im.jugadoresDisponibles j " +
        "WHERE im.mercado.ligaFantasy.id = :ligaId AND im.resuelta = false)")
    List<JugadorFantasy> findDisponiblesParaAsignacion(@Param("ligaId") Integer ligaId);


    // Elimina contarPuestasEnVenta del PujaRepository y añade esto en JugadorFantasyRepository:
    @Query("SELECT COUNT(jf) FROM JugadorFantasy jf WHERE jf.jugadorReal.id = :jugadorId AND jf.enVenta = true")
    long contarPuestasEnVenta(@Param("jugadorId") Integer jugadorId);
}
