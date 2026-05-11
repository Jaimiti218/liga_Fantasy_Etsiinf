package com.ligainternaetsiinf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.AlineacionDTO;
import com.ligainternaetsiinf.dto.ClasificacionResponse;
import com.ligainternaetsiinf.dto.EquipoFantasyResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyResponse;
import com.ligainternaetsiinf.dto.PuntosJornadaResponse;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.EstadisticasJugadorPartido;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.Partido;
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
            resultado.add(new ClasificacionResponse(ef.getId(), ef.getUser().getUsername(), ef.getPuntos()));
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
                jf.getClausulaBloqueadaHasta(), jf.isAlineado() // ← directo
            ));
        }
        return resultado;
    }

    public List<PuntosJornadaResponse> obtenerPuntosJornada(Integer equipoId, Integer jornada) {
        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // Jugadores alineados usando el nuevo atributo boolean
        List<JugadorFantasy> alineados = jugadorFantasyRepository
            .findByEquipoFantasyIdAndAlineadoTrue(equipoId);

        if (alineados.isEmpty()) return new ArrayList<>();

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
                        goles        += e.getGoles();
                        asistencias  += e.getAsistencias();
                        amarillas    += e.getTarjetasAmarillas();
                        rojas        += e.getTarjetasRojas();
                        paradas      += e.getParadas();
                        jugo          = e.isJugo();
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
