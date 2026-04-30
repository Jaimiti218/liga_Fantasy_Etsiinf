package com.ligainternaetsiinf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ligainternaetsiinf.model.Jugador;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {
    
    Optional<Jugador> findByFullName(String fullName);
}
