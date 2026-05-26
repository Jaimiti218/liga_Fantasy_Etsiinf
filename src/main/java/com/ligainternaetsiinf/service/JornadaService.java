package com.ligainternaetsiinf.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.JornadaRequest;
import com.ligainternaetsiinf.dto.JornadaResponse;
import com.ligainternaetsiinf.model.Jornada;
import com.ligainternaetsiinf.repository.AlineacionEquipoJornadaRepository;
import com.ligainternaetsiinf.repository.JornadaRepository;

@Service
public class JornadaService {

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

    private void programarRegistroAlineaciones(Jornada jornada) {
        if (jornada.getFechaInicio() == null) return;
        if (jornada.getFechaInicio().isBefore(java.time.LocalDateTime.now())) return;

        // Cancelar tarea anterior si existe
        java.util.concurrent.ScheduledFuture<?> tareaAnterior = 
            tareasProgramadas.get(jornada.getNumero());
        if (tareaAnterior != null) {
            tareaAnterior.cancel(false);
        }

        Instant fechaEjecucion = jornada.getFechaInicio()
            .atZone(ZoneId.systemDefault()).toInstant();
        Integer numero = jornada.getNumero();

        java.util.concurrent.ScheduledFuture<?> tarea = taskScheduler.schedule(() -> {
            boolean yaRegistrado = !alineacionRepository.findByJornada(numero).isEmpty();
            if (!yaRegistrado) {
                equipoFantasyService.registrarAlineacionesJornada(numero);
            }
            tareasProgramadas.remove(numero);
        }, fechaEjecucion);

        tareasProgramadas.put(jornada.getNumero(), tarea);
    }

    
}