package com.ligainternaetsiinf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ligainternaetsiinf.model.Equipo;
import com.ligainternaetsiinf.model.Jugador;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    Optional<Equipo> findByName(String name);

}