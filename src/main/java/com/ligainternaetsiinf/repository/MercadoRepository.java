package com.ligainternaetsiinf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ligainternaetsiinf.model.Mercado;
import com.ligainternaetsiinf.model.LigaFantasy;

import java.util.Optional;

public interface MercadoRepository extends JpaRepository<Mercado, Integer> {

    Optional<Mercado> findByLigaFantasyId(Integer ligaId);

}