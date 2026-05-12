package com.ligainternaetsiinf.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.ClasificacionResponse;
import com.ligainternaetsiinf.dto.EquipoFantasyResponse;
import com.ligainternaetsiinf.security.CustomUserDetails;
import com.ligainternaetsiinf.service.EquipoFantasyService;
import com.ligainternaetsiinf.dto.AlineacionDTO;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.PuntosJornadaJugadorResponse;
import com.ligainternaetsiinf.dto.PuntosJornadaResponse;


@RestController
@RequestMapping("/equipos-fantasy")
public class EquipoFantasyController {

    @Autowired
    private EquipoFantasyService equipoFantasyService;

    @GetMapping("/{equipoId}")
    public EquipoFantasyResponse obtenerEquipo(@PathVariable Integer equipoId){
        return equipoFantasyService.obtenerEquipo(equipoId);
    }

    /*creamos lo de clasificacion response para que en el frontend no se vean datos innecesarios */
    @GetMapping("/ligas/{ligaId}/clasificacion")
    public List<ClasificacionResponse> obtenerClasificacionLiga(@PathVariable Integer ligaId) {
        return equipoFantasyService.obtenerClasificacionLiga(ligaId);
    }
    

    @GetMapping("/liga/{ligaId}")
    public List<EquipoFantasyResponse> obtenerEquiposDeLiga(@PathVariable Integer ligaId){
        return equipoFantasyService.obtenerEquiposDeLiga(ligaId);
    }

    @DeleteMapping("/{equipoId}")
    public void eliminarEquipo(@PathVariable Integer equipoId){
        equipoFantasyService.eliminarEquipo(equipoId);
    }


    /*este metodo y este endpoint lo usamos para que cuando un usuario vea sus ligas, le aparezca cuantos puntos dinero y demas tiene en cada liga,
    para lo que necesitamos acceder al su equipoFantasy dado un ligaId */
    @GetMapping("/liga/{ligaId}/mi-equipo")
    public EquipoFantasyResponse obtenerMiEquipo(@PathVariable Integer ligaId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return equipoFantasyService.obtenerEquipoDeUsuarioEnLiga(ligaId, userDetails.getId());
    }




    @PutMapping("/{equipoId}/alineacion")
    public EquipoFantasyResponse guardarAlineacion(
            @PathVariable Integer equipoId,
            @RequestBody AlineacionDTO dto,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Verificar que el equipo pertenece al usuario
        EquipoFantasyResponse equipo = equipoFantasyService.obtenerEquipo(equipoId);
        if (!equipo.getUserId().equals(userDetails.getId())) {
            throw new RuntimeException("No tienes permiso para modificar este equipo");
        }
        return equipoFantasyService.guardarAlineacion(equipoId, dto);
    }

    @GetMapping("/{equipoId}/plantilla")
    public List<JugadorFantasyDetalleResponse> obtenerPlantilla(@PathVariable Integer equipoId) {
        return equipoFantasyService.obtenerPlantillaDetalle(equipoId);
    }

    @GetMapping("/{equipoId}/puntos/jornada/{jornada}")
    public List<PuntosJornadaResponse> obtenerPuntosJornada(
            @PathVariable Integer equipoId,
            @PathVariable Integer jornada) {
        return equipoFantasyService.obtenerPuntosJornada(equipoId, jornada);
    }

    @GetMapping("/jugador/{jugadorFantasyId}/jornadas")
    public List<PuntosJornadaJugadorResponse> obtenerPuntosJugadorPorJornadas(
            @PathVariable Integer jugadorFantasyId) {
        return equipoFantasyService.obtenerPuntosJugadorPorJornadas(jugadorFantasyId);
    }
}
