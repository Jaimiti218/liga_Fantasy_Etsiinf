package com.ligainternaetsiinf.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.JornadaRequest;
import com.ligainternaetsiinf.dto.JornadaResponse;
import com.ligainternaetsiinf.model.Jornada;
import com.ligainternaetsiinf.repository.AlineacionEquipoJornadaRepository;
import com.ligainternaetsiinf.repository.JornadaRepository;

@Service
public class JornadaService {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    @Autowired private JornadaRepository jornadaRepository;
    @Autowired private EquipoFantasyService equipoFantasyService;
    @Autowired private AlineacionEquipoJornadaRepository alineacionRepository;
    @Autowired private TaskScheduler taskScheduler;

    private final Map<Integer, java.util.concurrent.ScheduledFuture<?>> tareasProgramadas 
    = new java.util.concurrent.ConcurrentHashMap<>();

    public List<JornadaResponse> listar() {
        return jornadaRepository.findAllByOrderByNumeroAsc()
            .stream()
            .map(j -> new JornadaResponse(j.getId(), j.getNumero(), j.getFechaInicio()))
            .collect(Collectors.toList());
    }

    public JornadaResponse crear(JornadaRequest request) {
        if (jornadaRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Ya existe la jornada " + request.getNumero());
        }
        Jornada jornada = new Jornada(request.getNumero(), request.getFechaInicio());
        jornadaRepository.save(jornada);
        programarRegistroAlineaciones(jornada);
        return new JornadaResponse(jornada.getId(), jornada.getNumero(), jornada.getFechaInicio());
    }

    public JornadaResponse editar(Integer id, JornadaRequest request) {
        Jornada jornada = jornadaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jornada no encontrada"));
        jornada.setFechaInicio(request.getFechaInicio());
        jornadaRepository.save(jornada);
        // Reprogramar el registro de alineaciones con la nueva fecha
        programarRegistroAlineaciones(jornada);
        return new JornadaResponse(jornada.getId(), jornada.getNumero(), jornada.getFechaInicio());
    }

    public void eliminar(Integer id) {
        Jornada jornada = jornadaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jornada no encontrada"));
        
        java.util.concurrent.ScheduledFuture<?> tarea = 
            tareasProgramadas.get(jornada.getNumero());
        if (tarea != null) {
            tarea.cancel(false);
            tareasProgramadas.remove(jornada.getNumero());
        }
        
        jornadaRepository.deleteById(id);
    }

    // ─── Al arrancar el servidor ─────────────────────────────────────────────
    // Las tareas del TaskScheduler viven en memoria y se pierden al reiniciar.
    // Las jornadas sí están en BD, así que al arrancar:
    //  1) Se vuelven a programar todas las jornadas cuya fecha de inicio es futura.
    //  2) Si la última jornada ya comenzada no tiene NINGUNA alineación registrada
    //     (el servidor estaba parado en su hora de inicio), se registran ahora.
    @EventListener(ApplicationReadyEvent.class)
    public void reprogramarAlArrancar() {
        LocalDateTime ahora = LocalDateTime.now(ZONA);
        Jornada ultimaComenzada = null;

        for (Jornada jornada : jornadaRepository.findAllByOrderByNumeroAsc()) {
            if (jornada.getFechaInicio() == null) continue;
            if (jornada.getFechaInicio().isAfter(ahora)) {
                programarRegistroAlineaciones(jornada);
            } else if (ultimaComenzada == null
                    || jornada.getFechaInicio().isAfter(ultimaComenzada.getFechaInicio())) {
                ultimaComenzada = jornada;
            }
        }

        if (ultimaComenzada != null) {
            Integer numero = ultimaComenzada.getNumero();
            try {
                if (alineacionRepository.findByJornada(numero).isEmpty()) {
                    System.out.println("Registrando alineaciones pendientes de la jornada " + numero);
                    equipoFantasyService.registrarAlineacionesJornada(numero);
                }
            } catch (Exception e) {
                System.err.println("Error registrando alineaciones de la jornada " + numero
                    + ": " + e.getMessage());
            }
        }
    }

    private void programarRegistroAlineaciones(Jornada jornada) {
        if (jornada.getFechaInicio() == null) return;

        // Cancelar tarea anterior si existe (p. ej. al editar la fecha)
        java.util.concurrent.ScheduledFuture<?> tareaAnterior = 
            tareasProgramadas.remove(jornada.getNumero());
        if (tareaAnterior != null) {
            tareaAnterior.cancel(false);
        }

        if (jornada.getFechaInicio().isBefore(LocalDateTime.now(ZONA))) return;

        Instant fechaEjecucion = jornada.getFechaInicio().atZone(ZONA).toInstant();
        Integer numero = jornada.getNumero();

        java.util.concurrent.ScheduledFuture<?> tarea = taskScheduler.schedule(() -> {
            try {
                boolean yaRegistrado = !alineacionRepository.findByJornada(numero).isEmpty();
                if (!yaRegistrado) {
                    equipoFantasyService.registrarAlineacionesJornada(numero);
                }
            } catch (Exception e) {
                System.err.println("Error registrando alineaciones de la jornada " + numero
                    + ": " + e.getMessage());
            } finally {
                tareasProgramadas.remove(numero);
            }
        }, fechaEjecucion);

        tareasProgramadas.put(numero, tarea);
    }

    
}
