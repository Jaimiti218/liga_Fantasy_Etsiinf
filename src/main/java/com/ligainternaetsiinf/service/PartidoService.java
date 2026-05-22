package com.ligainternaetsiinf.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.EstadisticasPartidoResponse;
import com.ligainternaetsiinf.dto.PartidoRequest;
import com.ligainternaetsiinf.dto.PartidoResponse;
import com.ligainternaetsiinf.dto.ResultadoRequest;
import com.ligainternaetsiinf.model.AlineacionEquipoJornada;
import com.ligainternaetsiinf.model.Equipo;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.EstadisticasJugadorPartido;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.Partido;
import com.ligainternaetsiinf.repository.AlineacionEquipoJornadaRepository;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;
import com.ligainternaetsiinf.repository.EquipoRepository;
import com.ligainternaetsiinf.repository.EstadisticasJugadorPartidoRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.JugadorRepository;
import com.ligainternaetsiinf.repository.PartidoRepository;

@Service
public class PartidoService {

    @Autowired private PartidoRepository partidoRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private JugadorFantasyRepository jugadorFantasyRepository;
    @Autowired private EquipoFantasyRepository equipoFantasyRepository;
    @Autowired private EstadisticasJugadorPartidoRepository estadisticasRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private EquipoFantasyService equipoFantasyService;
    @Autowired private AlineacionEquipoJornadaRepository alineacionRepository;
    @Autowired private TaskScheduler taskScheduler;

    public PartidoResponse crearPartido(PartidoRequest request) {
        Equipo local = equipoRepository.findById(request.getEquipoLocalId())
            .orElseThrow(() -> new RuntimeException("Equipo local no encontrado"));
        Equipo visitante = equipoRepository.findById(request.getEquipoVisitanteId())
            .orElseThrow(() -> new RuntimeException("Equipo visitante no encontrado"));

        if (local.getId().equals(visitante.getId())) {
            throw new RuntimeException("Un equipo no puede jugar contra sí mismo");
        }

        Partido partido = new Partido(local, visitante, request.getFecha(), request.getJornada());
        partidoRepository.save(partido);

        // Programar el registro de alineaciones para cuando empiece este partido SOLO SI ES EL PRIMERO DE LA JORNADA
        if(request.getEsPrimero()){
            programarRegistroAlineaciones(partido);
        }
        

        return toResponse(partido);
    }

    public PartidoResponse editarPartido(Integer id, PartidoRequest request) {
        Partido partido = partidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        if (request.getFecha() != null) {
            partido.setFecha(request.getFecha());
        }
        if (request.getJornada() != null) partido.setJornada(request.getJornada());

        partidoRepository.save(partido);
        return toResponse(partido);
    }

    public void eliminarPartido(Integer id) {
        if (!partidoRepository.existsById(id)) {
            throw new RuntimeException("Partido no encontrado");
        }
        partidoRepository.deleteById(id);
    }

    public List<PartidoResponse> listarPartidos() {
        return partidoRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<PartidoResponse> listarPartidosPorJornada(Integer jornada) {
        return partidoRepository.findByJornada(jornada)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public PartidoResponse registrarResultado(Integer id, ResultadoRequest request) {
        Partido partido = partidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        if (partido.isJugado()) {
            throw new RuntimeException("Este partido ya tiene resultado registrado");
        }

        partido.setGolesLocal(request.getGolesLocal());
        partido.setGolesVisitante(request.getGolesVisitante());
        partido.setJugado(true);
        partidoRepository.save(partido);

        // Guardar estadísticas de cada jugador
        for (ResultadoRequest.EstadisticaJugadorRequest stat : request.getEstadisticas()) {
            if (!stat.isJugo()) continue;

            Jugador jugador = jugadorRepository.findById(stat.getJugadorId())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + stat.getJugadorId()));

            // Calcular puntos de este jugador en este partido
            int puntos = calcularPuntosJugador(jugador.getPosicion(), stat,
                request.getGolesLocal(), request.getGolesVisitante(), partido);

            // Guardar/actualizar estadísticas del partido
            EstadisticasJugadorPartido estadistica = estadisticasRepository
                .findByJugadorIdAndPartidoId(jugador.getId(), partido.getId())
                .orElse(new EstadisticasJugadorPartido(jugador, partido));

            estadistica.setJugo(true);
            estadistica.setGoles(stat.getGoles());
            estadistica.setAsistencias(stat.getAsistencias());
            estadistica.setTarjetasAmarillas(stat.getTarjetasAmarillas());
            estadistica.setTarjetasRojas(stat.getTarjetasRojas());
            estadistica.setParadas(stat.getParadas());
            estadistica.setPuntosObtenidos(puntos);
            estadisticasRepository.save(estadistica);

            // Actualizar estadísticas acumuladas del jugador real
            jugador.setPuntosFantasy(jugador.getPuntosFantasy() + puntos);
            jugador.setGoles(jugador.getGoles() + stat.getGoles());
            jugador.setAsistencias(jugador.getAsistencias() + stat.getAsistencias());
            jugador.setTarjetasAmarillas(jugador.getTarjetasAmarillas() + stat.getTarjetasAmarillas());
            jugador.setTarjetasRojas(jugador.getTarjetasRojas() + stat.getTarjetasRojas());
            jugador.setParadas(jugador.getParadas() + stat.getParadas());
            jugador.setPartidosJugados(jugador.getPartidosJugados() + 1);
            jugadorRepository.save(jugador);
        }

        // Otorgar puntos a los equipos fantasy que tenían alineados a estos jugadores
        otorgarPuntosFantasy(partido);

        return toResponse(partido);
    }

    private int calcularPuntosJugador(String posicion,
        ResultadoRequest.EstadisticaJugadorRequest stat,
        int golesLocal, int golesVisitante, Partido partido) {

        int puntos = 2; // puntos base por jugar

        // Determinar goles encajados por el equipo de este jugador
        boolean esLocal = partido.getEquipoLocal().getJugadores().stream()
            .anyMatch(j -> j.getId().equals(stat.getJugadorId()));
        int golesEncajados = esLocal ? golesVisitante : golesLocal;

        // Puntos por portería / goles encajados según posición
        switch (posicion) {
            case "PORTERO" -> {
                int base = 7;
                int resta = golesEncajados; // -1 por cada gol
                puntos += base-resta; // nunca resta más de lo que da
            }
            case "DEFENSA" -> {
                int base = 5;
                int resta = golesEncajados / 2; // -1 cada 2 goles
                puntos += base-resta;
            }
            case "MEDIOCENTRO" -> {
                int base = 2;
                int resta = golesEncajados / 3; // -1 cada 3 goles
                puntos += base-resta;
            }
            case "DELANTERO" -> {
                int base = 1;
                int resta = golesEncajados / 4; // -1 cada 4 goles
                puntos += base-resta;
            }
        }

        // Goles: portero/defensa 6pts, mediocentro 5pts, delantero 4pts
        int ptsGol = switch (posicion) {
            case "PORTERO", "DEFENSA" -> 6;
            case "MEDIOCENTRO"        -> 5;
            default                   -> 4;
        };
        puntos += stat.getGoles() * ptsGol;

        // Asistencias: 3pts
        puntos += stat.getAsistencias() * 3;

        // Tarjetas
        puntos -= stat.getTarjetasAmarillas();
        puntos -= stat.getTarjetasRojas() * 3;

        // Portero: paradas
        if ("PORTERO".equals(posicion)) {
            puntos += stat.getParadas();
        }

        return puntos; // puede ser negativo
    }

    private void otorgarPuntosFantasy(Partido partido) {
        Integer jornada = partido.getJornada();

        // Obtener todas las alineaciones de esta jornada
        List<AlineacionEquipoJornada> alineaciones = alineacionRepository.findByJornada(jornada);

        for (AlineacionEquipoJornada alineacion : alineaciones) {
            if (!alineacion.isPuntua()) continue;

            EquipoFantasy equipo = alineacion.getEquipoFantasy();
            int puntosJornada = 0;

            for (JugadorFantasy jf : alineacion.getJugadoresAlineados()) {
                Jugador jr = jf.getJugadorReal();
                if (jr.getEquipo() == null) continue;

                // ¿Jugó este jugador en este partido?
                if (!jr.getEquipo().getId().equals(partido.getEquipoLocal().getId()) &&
                    !jr.getEquipo().getId().equals(partido.getEquipoVisitante().getId())) {
                    continue;
                }

                var stats = estadisticasRepository
                    .findByJugadorIdAndPartidoId(jr.getId(), partido.getId());

                if (stats.isPresent() && stats.get().isJugo()) {
                    puntosJornada += stats.get().getPuntosObtenidos();
                }
            }

            // Sumar puntos al equipo fantasy
            equipo.setPuntos(equipo.getPuntos() + puntosJornada);
            equipoFantasyRepository.save(equipo);
        }
    }

    // ─── Programar registro de alineaciones ──────────────────────────────────
    private void programarRegistroAlineaciones(Partido partido) {
        if (partido.getFecha() == null || partido.getJornada() == null) return;

        if (partido.getFecha().isBefore(java.time.LocalDateTime.now())) return;

        Instant fechaEjecucion = partido.getFecha()
            .atZone(ZoneId.systemDefault())
            .toInstant();

        Integer jornada = partido.getJornada();

        taskScheduler.schedule(() -> {
            boolean yaRegistrado = !alineacionRepository.findByJornada(jornada).isEmpty();
            if (!yaRegistrado) {
                System.out.println("Registrando alineaciones para jornada " + jornada);
                equipoFantasyService.registrarAlineacionesJornada(jornada);
            }
        }, fechaEjecucion);
    }

    public List<EstadisticasPartidoResponse> getEstadisticasPartido(Integer partidoId) {
        List<EstadisticasJugadorPartido> stats = estadisticasRepository.findByPartidoId(partidoId);
        return stats.stream()
            .filter(EstadisticasJugadorPartido::isJugo)
            .map(e -> new EstadisticasPartidoResponse(
                e.getJugador().getId(),
                e.getJugador().getFullName(),
                e.getJugador().getPosicion(),
                e.getJugador().getEquipo() != null ? e.getJugador().getEquipo().getName() : null,
                e.getGoles(), e.getAsistencias(),
                e.getTarjetasAmarillas(), e.getTarjetasRojas(),
                e.getParadas(), e.isJugo()
            ))
            .collect(java.util.stream.Collectors.toList());
    }

    private PartidoResponse toResponse(Partido p) {
        return new PartidoResponse(
            p.getId(),
            p.getEquipoLocal().getId(),
            p.getEquipoLocal().getName(),
            p.getEquipoVisitante().getId(),
            p.getEquipoVisitante().getName(),
            p.getFecha(),
            p.getJornada(),
            p.getGolesLocal(),
            p.getGolesVisitante(),
            p.isJugado()
        );
    }
}
