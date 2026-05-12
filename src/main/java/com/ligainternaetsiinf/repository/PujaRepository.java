package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.Puja;
import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.EquipoFantasy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PujaRepository extends JpaRepository<Puja, Integer> {
    List<Puja> findByInstancia(InstanciaMercado instancia);
    List<Puja> findByJugadorFantasyAndInstancia(JugadorFantasy jugador, InstanciaMercado instancia);
    List<Puja> findByEquipoFantasyAndInstancia(EquipoFantasy equipo, InstanciaMercado instancia);
    Optional<Puja> findByEquipoFantasyAndJugadorFantasyAndInstancia(EquipoFantasy equipo, JugadorFantasy jugador, InstanciaMercado instancia);
}