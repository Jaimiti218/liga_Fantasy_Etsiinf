package com.ligainternaetsiinf.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.PartidoRequest;
import com.ligainternaetsiinf.dto.PartidoResponse;
import com.ligainternaetsiinf.model.Equipo;
import com.ligainternaetsiinf.model.Partido;
import com.ligainternaetsiinf.repository.AlineacionEquipoJornadaRepository;
import com.ligainternaetsiinf.repository.EquipoRepository;
import com.ligainternaetsiinf.repository.PartidoRepository;

@Service
public class PartidoService {

    @Autowired private PartidoRepository partidoRepository;
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

        // Programar el registro de alineaciones para cuando empiece este partido
        programarRegistroAlineaciones(partido);

        return toResponse(partido);
    }

    public PartidoResponse editarPartido(Integer id, PartidoRequest request) {
        Partido partido = partidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        if (request.getFecha() != null) {
            partido.setFecha(request.getFecha());
            // Reprogramar la tarea con la nueva fecha
            programarRegistroAlineaciones(partido);
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

    public PartidoResponse registrarResultado(Integer id, Integer golesLocal, Integer golesVisitante) {
        Partido partido = partidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);
        partido.setJugado(true);
        partidoRepository.save(partido);

        return toResponse(partido);
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
