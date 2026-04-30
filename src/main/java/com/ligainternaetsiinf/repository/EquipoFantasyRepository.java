package com.ligainternaetsiinf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.model.User;

import java.util.List;
import java.util.Optional;

public interface EquipoFantasyRepository extends JpaRepository<EquipoFantasy, Integer> {

    List<EquipoFantasy> findByLigaFantasyIdOrderByPuntosDesc(Integer ligaId);

    List<EquipoFantasy> findByLigaFantasyId(Integer ligaId);

    boolean existsByLigaFantasyAndUser(LigaFantasy ligaFantasy, User user);
   
    long countByLigaFantasy(LigaFantasy ligaFantasy);
    
    Optional<EquipoFantasy> findByLigaFantasyIdAndUserId(Integer ligaId, Integer userId);

    @Query("""
    SELECT ef.ligaFantasy
    FROM EquipoFantasy ef
    WHERE ef.user.id = :userId
    """)
    List<LigaFantasy> findLigasByUserId(Integer userId);

}