package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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



    // Compras al sistema: aceptada, sin vendedor previo (jugador libre)
    @Query("SELECT COUNT(p) FROM Puja p WHERE p.jugadorFantasy.jugadorReal.id = :jugadorId " +
        "AND p.aceptada = true AND p.equipoVendedor IS NULL AND p.equipoComprador IS NOT NULL " +
        "AND p.esClausulazo = false AND p.fecha >= :desde")
    long contarComprasAlSistema(@Param("jugadorId") Integer jugadorId,
                                @Param("desde") LocalDateTime desde);

    // Ventas al sistema: oferta del sistema aceptada (comprador null)
    @Query("SELECT COUNT(p) FROM Puja p WHERE p.jugadorFantasy.jugadorReal.id = :jugadorId " +
        "AND p.aceptada = true AND p.equipoComprador IS NULL " +
        "AND p.fecha >= :desde")
    long contarVentasAlSistema(@Param("jugadorId") Integer jugadorId,
                                @Param("desde") LocalDateTime desde);

    // Pujas activas no resueltas con comprador real
    @Query("SELECT COUNT(p) FROM Puja p WHERE p.jugadorFantasy.jugadorReal.id = :jugadorId " +
        "AND p.resuelta = false AND p.equipoComprador IS NOT NULL " +
        "AND p.fecha >= :desde")
    long contarPujasActivas(@Param("jugadorId") Integer jugadorId,
                            @Param("desde") LocalDateTime desde);

    

    // Clausulazos ejecutados
    @Query("SELECT p FROM Puja p WHERE p.jugadorFantasy.jugadorReal.id = :jugadorId " +
        "AND p.esClausulazo = true AND p.aceptada = true " +
        "AND p.fecha >= :desde")
    List<Puja> findClausulazos(@Param("jugadorId") Integer jugadorId,
                                @Param("desde") LocalDateTime desde);
}