package com.ligainternaetsiinf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ligainternaetsiinf.dto.InstanciaMercadoResponse;
import com.ligainternaetsiinf.dto.PujaResponse;
import com.ligainternaetsiinf.dto.VentaResponse;
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
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorUsuarioYInstancia(jugadorFantasyId, ud.getId());
        return mercadoService.realizarPuja(equipoId, jugadorFantasyId, instanciaId, cantidad);
    }

    @PostMapping("/oferta-directa")
    public void hacerOfertaDirecta(
            @RequestParam Integer jugadorFantasyId,
            @RequestParam long cantidad,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        mercadoService.hacerOfertaDirecta(jugadorFantasyId, cantidad, ud.getId());
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

    @PostMapping("/pujas/{pujaId}/aceptar")
    public void aceptarPuja(@PathVariable Integer pujaId, Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        mercadoService.aceptarPuja(pujaId, ud.getId());
    }

    @GetMapping("/mis-pujas")
    public List<PujaResponse> misPujas(
            @RequestParam Integer ligaId,
            Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        Integer equipoId = equipoFantasyService
            .obtenerEquipoIdPorUsuarioYLiga(ligaId, ud.getId());
        return mercadoService.obtenerMisPujas(equipoId);
    }

    @GetMapping("/mis-ventas")
    public List<VentaResponse> misVentas(
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

    @PostMapping("/pujas/{pujaId}/rechazar")
    public void rechazarPuja(@PathVariable Integer pujaId, Authentication authentication) {
        CustomUserDetails ud = (CustomUserDetails) authentication.getPrincipal();
        mercadoService.rechazarPuja(pujaId, ud.getId());
    }
}