package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.Jornada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface JornadaRepository extends JpaRepository<Jornada, Integer> {
    Optional<Jornada> findByNumero(Integer numero);
    List<Jornada> findAllByOrderByNumeroAsc();
    boolean existsByNumero(Integer numero);
}
