package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PujaRepository extends JpaRepository<Puja, Integer> {

    // Pujas en una instancia de mercado por jugador
    List<Puja> findByJugadorFantasyAndInstancia(JugadorFantasy jugador, InstanciaMercado instancia);

    // Todas las pujas de una instancia
    List<Puja> findByInstancia(InstanciaMercado instancia);

    // Pujas activas del comprador en una instancia
    List<Puja> findByEquipoCompradorAndInstancia(EquipoFantasy equipo, InstanciaMercado instancia);

    // Puja concreta de un comprador por un jugador en una instancia
    Optional<Puja> findByEquipoCompradorAndJugadorFantasyAndInstancia(
        EquipoFantasy equipo, JugadorFantasy jugador, InstanciaMercado instancia);

    // Pujas no resueltas sobre un jugador (para mis ventas)
    List<Puja> findByJugadorFantasyAndResueltaFalse(JugadorFantasy jugador);

    // Pujas no resueltas del comprador (para mis compras)
    List<Puja> findByEquipoCompradorAndResueltaFalse(EquipoFantasy equipo);

    List<Puja> findByEquipoVendedorAndResueltaFalse(EquipoFantasy equipo);
    
    //ESTOS DOS METODOS LOS USAREMOS SI QUEREMOS BORRAR TODAS LAS PUJAS DE ESE EQUIPO EN ESA LIGA
    List<Puja> findByEquipoComprador(EquipoFantasy equipo);

    List<Puja> findByEquipoVendedor(EquipoFantasy equipo);

    // Puja directa entre dos equipos por un jugador sin instancia
    Optional<Puja> findByEquipoCompradorAndJugadorFantasyAndInstanciaIsNull(
        EquipoFantasy equipo, JugadorFantasy jugador);


    Optional<Puja> findByEquipoCompradorAndJugadorFantasyAndInstanciaIsNullAndResueltaFalse(EquipoFantasy equipo, JugadorFantasy jugador);

    Optional<Puja> findByEquipoCompradorAndJugadorFantasyAndInstanciaAndResueltaFalse(EquipoFantasy equipo, JugadorFantasy jugador, InstanciaMercado instancia);
}