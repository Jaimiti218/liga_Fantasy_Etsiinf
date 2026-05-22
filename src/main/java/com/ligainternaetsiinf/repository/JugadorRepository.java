package com.ligainternaetsiinf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ligainternaetsiinf.model.Jugador;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {
    
    @Query(value = "SELECT * FROM jugador WHERE full_name = :fullName LIMIT 1", nativeQuery = true)
    Optional<Jugador> findByFullName(@Param("fullName") String fullName);
}
