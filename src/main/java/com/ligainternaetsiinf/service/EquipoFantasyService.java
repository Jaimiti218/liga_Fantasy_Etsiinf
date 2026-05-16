package com.ligainternaetsiinf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.ligainternaetsiinf.dto.AlineacionDTO;
import com.ligainternaetsiinf.dto.ClasificacionResponse;
import com.ligainternaetsiinf.dto.EquipoFantasyResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyResponse;
import com.ligainternaetsiinf.dto.PuntosJornadaJugadorResponse;
import com.ligainternaetsiinf.dto.PuntosJornadaResponse;
import com.ligainternaetsiinf.model.AlineacionEquipoJornada;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.EstadisticasJugadorPartido;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.Partido;
import com.ligainternaetsiinf.repository.AlineacionEquipoJornadaRepository;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;
import com.ligainternaetsiinf.repository.EstadisticasJugadorPartidoRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.PartidoRepository;
import com.ligainternaetsiinf.dto.AlineacionDTO;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.PuntosJornadaResponse;
import com.ligainternaetsiinf.model.EstadisticasJugadorPartido;
import com.ligainternaetsiinf.model.Partido;
import com.ligainternaetsiinf.repository.EstadisticasJugadorPartidoRepository;
import com.ligainternaetsiinf.repository.PartidoRepository;

@Service
public class EquipoFantasyService {

    @Autowired
    private EquipoFantasyRepository equipoFantasyRepository;

    @Autowired
    private JugadorFantasyRepository jugadorFantasyRepository;

    @Autowired
    private EstadisticasJugadorPartidoRepository estadisticasRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private AlineacionEquipoJornadaRepository alineacionRepository;

    public EquipoFantasyResponse obtenerEquipo(Integer equipoId){

        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        return cambiarTipoRespuesta(equipo);
    }

    public List<EquipoFantasyResponse> obtenerEquiposDeLiga(Integer ligaId){

        List<EquipoFantasy> equipos = equipoFantasyRepository.findByLigaFantasyId(ligaId);

        List<EquipoFantasyResponse> resultado = new ArrayList<>();

        for(EquipoFantasy ef : equipos){
            resultado.add(cambiarTipoRespuesta(ef));
        }

        return resultado;
    }

    /*creamos lo de clasificacion response para que en el frontend no se vean datos innecesarios */
    public List<ClasificacionResponse> obtenerClasificacionLiga(Integer ligaId){

        List<EquipoFantasy> equipos = equipoFantasyRepository.findByLigaFantasyIdOrderByPuntosDesc(ligaId);

        List<ClasificacionResponse> resultado = new ArrayList<>();

        for(EquipoFantasy ef : equipos){
            resultado.add(new ClasificacionResponse(
                ef.getId(),
                ef.getUser().getUsername(),
                ef.getPuntos(),
                ef.getUser().getFotoPerfil()
            ));
        }

        return resultado;
    }

    public void eliminarEquipo(Integer equipoId){

        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));


        for(JugadorFantasy j : equipo.getJugadores()){
            j.setEquipoFantasy(null);
            jugadorFantasyRepository.save(j);
        }
        equipoFantasyRepository.delete(equipo);
    }

    public EquipoFantasyResponse obtenerEquipoDeUsuarioEnLiga(Integer ligaId, Integer userId) {
        EquipoFantasy equipo = equipoFantasyRepository.findByLigaFantasyIdAndUserId(ligaId, userId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        return cambiarTipoRespuesta(equipo);
    }

    public EquipoFantasyResponse guardarAlineacion(Integer equipoId, AlineacionDTO dto) {
        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // Desalinear todos primero
        List<JugadorFantasy> todos = jugadorFantasyRepository.findByEquipoFantasyId(equipoId);
        for (JugadorFantasy jf : todos) {
            jf.setAlineado(false);
        }
        jugadorFantasyRepository.saveAll(todos);

        // Alinear los seleccionados (puede ser menos de 7)
        if (dto.getJugadorFantasyIds() != null) {
            for (Integer jfId : dto.getJugadorFantasyIds()) {
                JugadorFantasy jf = jugadorFantasyRepository.findById(jfId)
                    .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + jfId));
                if (!jf.getEquipoFantasy().getId().equals(equipoId)) {
                    throw new RuntimeException("El jugador no pertenece a este equipo");
                }
                jf.setAlineado(true);
                jugadorFantasyRepository.save(jf);
            }
        }

        if (dto.getFormacion() != null) {
            equipo.setFormacion(dto.getFormacion());
            equipoFantasyRepository.save(equipo);
        }

        return cambiarTipoRespuesta(equipo);
    }

    public List<JugadorFantasyDetalleResponse> obtenerPlantillaDetalle(Integer equipoId) {
        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        List<JugadorFantasy> jugadores = jugadorFantasyRepository.findByEquipoFantasyId(equipoId);
        List<JugadorFantasyDetalleResponse> resultado = new ArrayList<>();

        for (JugadorFantasy jf : jugadores) {
            Jugador jr = jf.getJugadorReal();
            int partidosJugados = jr.getPartidosJugados();
            double media = partidosJugados > 0 ? (double) jr.getPuntosFantasy() / partidosJugados : 0.0;
            String nombreEquipo = jr.getEquipo() != null ? jr.getEquipo().getName() : null;

            resultado.add(new JugadorFantasyDetalleResponse(
                jf.getId(), jr.getId(), jr.getFullName(), jr.getPosicion(),
                jr.getValorMercado(), nombreEquipo, jr.getPuntosFantasy(),
                media, partidosJugados, jf.getClausula(),
                jf.getClausulaBloqueadaHasta(), jf.isAlineado(), null, 
                equipo.getUser().getId(),jf.isEnVenta()
            ));
        }
        return resultado;
    }

    public List<PuntosJornadaResponse> obtenerPuntosJornada(Integer equipoId, Integer jornada) {

        // Usar la alineación registrada para esa jornada, no la actual
        Optional<AlineacionEquipoJornada> alineacionOpt =
            alineacionRepository.findByEquipoFantasyIdAndJornada(equipoId, jornada);

        if (alineacionOpt.isEmpty()) return new ArrayList<>();

        AlineacionEquipoJornada alineacion = alineacionOpt.get();
        List<JugadorFantasy> alineados     = alineacion.getJugadoresAlineados();

        List<Partido> partidos = partidoRepository.findByJornada(jornada);
        List<PuntosJornadaResponse> resultado = new ArrayList<>();

        for (JugadorFantasy jf : alineados) {
            Jugador jr = jf.getJugadorReal();

            int puntosJornada = 0, goles = 0, asistencias = 0;
            int amarillas = 0, rojas = 0, paradas = 0;
            boolean jugo = false;

            for (Partido p : partidos) {
                if (jr.getEquipo() != null &&
                    (p.getEquipoLocal().getId().equals(jr.getEquipo().getId()) ||
                    p.getEquipoVisitante().getId().equals(jr.getEquipo().getId()))) {

                    var stats = estadisticasRepository
                        .findByJugadorIdAndPartidoId(jr.getId(), p.getId());

                    if (stats.isPresent()) {
                        EstadisticasJugadorPartido e = stats.get();
                        puntosJornada += e.getPuntosObtenidos();
                        goles         += e.getGoles();
                        asistencias   += e.getAsistencias();
                        amarillas     += e.getTarjetasAmarillas();
                        rojas         += e.getTarjetasRojas();
                        paradas       += e.getParadas();
                        jugo           = e.isJugo();
                    }
                }
            }

            resultado.add(new PuntosJornadaResponse(
                jf.getId(), jr.getFullName(), jr.getPosicion(),
                puntosJornada, goles, asistencias, amarillas, rojas, paradas, jugo
            ));
        }
        return resultado;
    }


    public List<PuntosJornadaJugadorResponse> obtenerPuntosJugadorPorJornadas(Integer jugadorFantasyId) {
        JugadorFantasy jf = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador fantasy no encontrado"));

        Jugador jr = jf.getJugadorReal();

        if (jr.getEquipo() == null) return new ArrayList<>();

        List<Partido> partidos = partidoRepository
            .findByEquipoLocalOrEquipoVisitanteOrderByJornadaAsc(jr.getEquipo(), jr.getEquipo());

        List<PuntosJornadaJugadorResponse> resultado = new ArrayList<>();

        for (Partido p : partidos) {
            if (!p.isJugado()) continue;
            var stats = estadisticasRepository.findByJugadorIdAndPartidoId(jr.getId(), p.getId());
            if (stats.isPresent()) {
                EstadisticasJugadorPartido e = stats.get();
                resultado.add(new PuntosJornadaJugadorResponse(
                    p.getJornada(), e.getPuntosObtenidos(), e.getGoles(),
                    e.getAsistencias(), e.getTarjetasAmarillas(), e.getTarjetasRojas(),
                    e.getParadas(), e.isJugo()
                ));
            }
        }
        return resultado;
    }


    public Integer obtenerEquipoIdPorUsuarioYLiga(Integer ligaId, Integer userId) {
        return equipoFantasyRepository.findByLigaFantasyIdAndUserId(ligaId, userId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"))
            .getId();
    }

    public Integer obtenerEquipoIdPorJugadorYUsuario(Integer jugadorFantasyId, Integer userId) {
        JugadorFantasy jf = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        if (!jf.getEquipoFantasy().getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso");
        }
        return jf.getEquipoFantasy().getId();
    }

    public Integer obtenerEquipoIdPorUsuarioYInstancia(Integer jugadorFantasyId, Integer userId) {
        JugadorFantasy jf = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        Integer ligaId = jf.getLigaFantasy().getId();
        return equipoFantasyRepository.findByLigaFantasyIdAndUserId(ligaId, userId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"))
            .getId();
    }

    public Integer obtenerEquipoIdPorOfertaYUsuario(Integer ofertaId, Integer userId) {
        // Este método necesita OfertaVentaRepository inyectado
        // Si no lo tienes en EquipoFantasyService, mueve la lógica al MercadoService
        throw new RuntimeException("Implementar con OfertaVentaRepository");
    }

    public void registrarAlineacionesJornada(Integer jornada) {
        List<EquipoFantasy> todosLosEquipos = equipoFantasyRepository.findAll();

        for (EquipoFantasy equipo : todosLosEquipos) {
            // Solo registrar si no existe ya para esta jornada
            if (alineacionRepository.findByEquipoFantasyIdAndJornada(equipo.getId(), jornada).isPresent()) {
                continue;
            }

            List<JugadorFantasy> alineados = jugadorFantasyRepository
                .findByEquipoFantasyIdAndAlineadoTrue(equipo.getId());

            if (alineados.isEmpty()) continue;

            AlineacionEquipoJornada registro = new AlineacionEquipoJornada(
                equipo, alineados, jornada, equipo.getFormacion()
            );
            alineacionRepository.save(registro);
        }
    }

    public Map<String, Object> obtenerInfoJornada(Integer equipoId, Integer jornada) {
        var alineacion = alineacionRepository.findByEquipoFantasyIdAndJornada(equipoId, jornada);
        Map<String, Object> info = new HashMap<>();
        info.put("formacion", alineacion.map(AlineacionEquipoJornada::getFormacion).orElse("2-3-1"));
        info.put("jugadores", obtenerPuntosJornada(equipoId, jornada));
        return info;
    }

    private EquipoFantasyResponse cambiarTipoRespuesta(EquipoFantasy equipo){

        List<String> jugadores = new ArrayList<>();

        for(JugadorFantasy jf : equipo.getJugadores()){
            jugadores.add(jf.getJugadorReal().getFullName());
        }

        return new EquipoFantasyResponse(
                equipo.getId(),
                equipo.getLigaFantasy().getId(),
                equipo.getLigaFantasy().getName(),
                equipo.getUser().getId(),
                equipo.getUser().getUsername(),
                equipo.getDinero(),
                equipo.getPuntos(),
                jugadores,
                equipo.getFormacion()
        );
    }
}
