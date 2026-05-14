package com.ligainternaetsiinf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.InstanciaMercadoResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.OfertaVentaResponse;
import com.ligainternaetsiinf.dto.PujaResponse;
import com.ligainternaetsiinf.security.CustomUserDetails;
import com.ligainternaetsiinf.service.EquipoFantasyService;
import com.ligainternaetsiinf.service.MercadoService;

@RestController
@RequestMapping("/mercado")
public class MercadoController {

    @Autowired private MercadoService mercadoService;
    @Autowired private EquipoFantasyService equipoFantasyService;

    @GetMapping("/liga/{ligaId}")
    public InstanciaMercadoResponse obtenerMercado(@PathVariable Integer ligaId) {
        return mercadoService.obtenerInstanciaActiva(ligaId);
    }

    @PostMapping("/pujar")
    public PujaResponse realizarPuja(
            @RequestParam Integer jugadorFantasyId,
            @RequestParam Integer instanciaId,
            @RequestParam long cantidad,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        // Obtener equipoId del usuario en esta liga
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorUsuarioYInstancia(jugadorFantasyId, ud.getId());
        return mercadoService.realizarPuja(equipoId, jugadorFantasyId, instanciaId, cantidad);
    }

    @PostMapping("/vender/{jugadorFantasyId}")
    public void ponerEnVenta(@PathVariable Integer jugadorFantasyId, Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorJugadorYUsuario(jugadorFantasyId, ud.getId());
        mercadoService.ponerEnVenta(jugadorFantasyId, equipoId);
    }

    @DeleteMapping("/vender/{jugadorFantasyId}")
    public void retirarDeVenta(@PathVariable Integer jugadorFantasyId, Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorJugadorYUsuario(jugadorFantasyId, ud.getId());
        mercadoService.retirarDeVenta(jugadorFantasyId, equipoId);
    }

    @PostMapping("/ventas/{ofertaId}/aceptar")
    public void aceptarOferta(@PathVariable Integer ofertaId, Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        mercadoService.aceptarOfertaVentaPorUsuario(ofertaId, ud.getId());
    }

    @GetMapping("/mis-pujas")
    public List<PujaResponse> misPujas(
            @RequestParam Integer ligaId,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorUsuarioYLiga(ligaId, ud.getId());
        return mercadoService.obtenerMisPujas(equipoId, ligaId);
    }

    @GetMapping("/mis-ventas")
    public List<JugadorFantasyDetalleResponse> misVentas(
            @RequestParam Integer ligaId,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorUsuarioYLiga(ligaId, ud.getId());
        return mercadoService.obtenerMisVentas(equipoId);
    }

    @DeleteMapping("/puja/{pujaId}")
    public void eliminarPuja(@PathVariable Integer pujaId, Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        mercadoService.eliminarPuja(pujaId, ud.getId());
    }

    @PutMapping("/puja/{pujaId}")
    public PujaResponse editarPuja(@PathVariable Integer pujaId,
            @RequestParam long cantidad,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        return mercadoService.editarPuja(pujaId, cantidad, ud.getId());
    }

    @GetMapping("/liga/{ligaId}/contadores-pujas")
    public Map<Integer, Long> obtenerContadorPujas(@PathVariable Integer ligaId) {
        return mercadoService.obtenerContadorPujas(ligaId);
    }
}
