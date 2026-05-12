package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InstanciaMercadoRepository extends JpaRepository<InstanciaMercado, Integer> {
    Optional<InstanciaMercado> findByMercadoAndResueltaFalse(Mercado mercado);
    List<InstanciaMercado> findByResueltaFalse();
}