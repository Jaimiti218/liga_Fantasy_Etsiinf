package com.ligainternaetsiinf.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.InstanciaMercadoResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.PujaResponse;
import com.ligainternaetsiinf.dto.VentaResponse;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.model.Mercado;
import com.ligainternaetsiinf.model.Puja;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;
import com.ligainternaetsiinf.repository.InstanciaMercadoRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.MercadoRepository;
import com.ligainternaetsiinf.repository.PujaRepository;

@Service
public class MercadoService {

    @Autowired private MercadoRepository mercadoRepository;
    @Autowired private InstanciaMercadoRepository instanciaRepository;
    @Autowired private JugadorFantasyRepository jugadorFantasyRepository;
    @Autowired private EquipoFantasyRepository equipoFantasyRepository;
    @Autowired private PujaRepository pujaRepository;
    @Autowired private NoticiaFantasyService noticiaService;
    @Autowired private TaskScheduler taskScheduler;

    // ─── Al arrancar: resolver vencidas y reprogramar futuras ─────────────────
   /*  @EventListener(ApplicationReadyEvent.class)
    public void resolverInstanciasPendientesAlArrancar() {
        List<InstanciaMercado> pendientes = instanciaRepository.findByResueltaFalse();
        LocalDateTime ahora = LocalDateTime.now();
        for (InstanciaMercado instancia : pendientes) {
            if (instancia.getFin().isBefore(ahora)) {
                resolverInstancia(instancia);
                crearNuevaInstancia(instancia);
            } else {
                programarResolucion(instancia);
            }
        }
    } 

    // ─── Scheduler horario como red de seguridad ──────────────────────────────
    @Scheduled(fixedDelay = 3600000)
    public void resolverInstanciasVencidas() {
        List<InstanciaMercado> pendientes = instanciaRepository.findByResueltaFalse();
        LocalDateTime ahora = LocalDateTime.now();
        for (InstanciaMercado instancia : pendientes) {
            if (instancia.getFin().isBefore(ahora)) {
                resolverInstancia(instancia);
                crearNuevaInstancia(instancia);
            }
        }
    } */

    // ─── Crear primera instancia ──────────────────────────────────────────────
    public void crearPrimeraInstancia(Mercado mercado) {
        List<JugadorFantasy> disponibles = obtenerJugadoresParaMercado(mercado.getLiga());
        InstanciaMercado instancia = new InstanciaMercado(mercado, disponibles);
        instanciaRepository.save(instancia);
        programarResolucion(instancia);
    }

    // ─── Obtener instancia activa ─────────────────────────────────────────────
    public InstanciaMercadoResponse obtenerInstanciaActiva(Integer ligaId) {
        Mercado mercado = mercadoRepository.findByLigaFantasyId(ligaId)
            .orElseThrow(() -> new RuntimeException("Mercado no encontrado"));
        InstanciaMercado instancia = instanciaRepository
            .findByMercadoAndResueltaFalse(mercado)
            .orElseThrow(() -> new RuntimeException("No hay instancia activa"));
        return toInstanciaResponse(instancia);
    }

    // ─── Realizar puja en mercado normal ──────────────────────────────────────
    public PujaResponse realizarPuja(Integer equipoCompradorId, Integer jugadorFantasyId,
            Integer instanciaId, long cantidad) {

        EquipoFantasy comprador = equipoFantasyRepository.findById(equipoCompradorId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        InstanciaMercado instancia = instanciaRepository.findById(instanciaId)
            .orElseThrow(() -> new RuntimeException("Instancia no encontrada"));

        boolean estaDisponible = instancia.getJugadoresDisponibles()
            .stream()
            .anyMatch(j -> j.getId().equals(jugador.getId()));

        if (!estaDisponible) {
            throw new RuntimeException("Este jugador no está disponible en el mercado actual");
        }

        if (cantidad < jugador.getJugadorReal().getValorMercado()) {
            throw new RuntimeException("La puja debe ser igual o mayor al valor de mercado del jugador (" 
                + formatearDinero(jugador.getJugadorReal().getValorMercado()) + ")");
        }

        validarSaldo(comprador, instancia, cantidad, null);

        EquipoFantasy vendedor = jugador.getEquipoFantasy();

        Optional<Puja> existente = pujaRepository
            .findByEquipoCompradorAndJugadorFantasyAndInstanciaAndResueltaFalse(comprador, jugador, instancia);

        Puja puja;
        if (existente.isPresent()) {
            puja = existente.get();
            puja.setCantidad(cantidad);
        } else {
            puja = new Puja(comprador, vendedor, jugador, instancia, cantidad);
        }
        pujaRepository.save(puja);
        return toPujaResponse(puja);
    }

    // ─── Oferta directa (desde plantilla de otro usuario) ────────────────────
    public void hacerOfertaDirecta(Integer jugadorFantasyId, long cantidad, Integer userId) {
        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        if (jugador.getEquipoFantasy() == null) {
            throw new RuntimeException("Este jugador no pertenece a ningún equipo");
        }
        if (jugador.getEquipoFantasy().getUser().getId().equals(userId)) {
            throw new RuntimeException("No puedes hacerte una oferta a ti mismo");
        }

        if (cantidad < jugador.getJugadorReal().getValorMercado()) {
            throw new RuntimeException("La puja debe ser igual o mayor al valor de mercado del jugador (" 
                + formatearDinero(jugador.getJugadorReal().getValorMercado()) + ")");
        }

        LigaFantasy liga = jugador.getLigaFantasy();
        Mercado mercado  = mercadoRepository.findByLigaFantasyId(liga.getId())
            .orElseThrow(() -> new RuntimeException("Mercado no encontrado"));
        InstanciaMercado instancia = instanciaRepository
            .findByMercadoAndResueltaFalse(mercado).orElse(null);

        EquipoFantasy comprador = equipoFantasyRepository
            .findByLigaFantasyIdAndUserId(liga.getId(), userId)
            .orElseThrow(() -> new RuntimeException("No perteneces a esta liga"));

        validarSaldo(comprador, instancia, cantidad, null);

        EquipoFantasy vendedor = jugador.getEquipoFantasy();

        // Buscar puja directa existente
        Optional<Puja> existente = pujaRepository
            .findByEquipoCompradorAndJugadorFantasyAndInstanciaIsNullAndResueltaFalse(comprador, jugador);

        Puja puja;
        if (existente.isPresent()) {
            puja = existente.get();
            puja.setCantidad(cantidad);
        } else {
            puja = new Puja(comprador, vendedor, jugador, cantidad);
        }

        // Notificar al vendedor que ha recibido una oferta
        Integer userIdVendedor = vendedor.getUser().getId();
        String compradorNombre = comprador.getUser().getUsername();
        String jugadorNombre   = jugador.getJugadorReal().getFullName();
        noticiaService.crearNoticiaOfertaRecibida(liga, userIdVendedor,
            compradorNombre, jugadorNombre);

        pujaRepository.save(puja);
    }

    // ─── Poner jugador en venta ───────────────────────────────────────────────
    public void ponerEnVenta(Integer jugadorFantasyId, Integer equipoId) {
        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        if (!jugador.getEquipoFantasy().getId().equals(equipoId)) {
            throw new RuntimeException("Este jugador no pertenece a tu equipo");
        }

        jugador.setEnVenta(true);
        jugadorFantasyRepository.save(jugador);

        // Añadir a la instancia activa
        LigaFantasy liga = jugador.getLigaFantasy();
        Mercado mercado  = mercadoRepository.findByLigaFantasyId(liga.getId()).orElse(null);
        if (mercado != null) {
            instanciaRepository.findByMercadoAndResueltaFalse(mercado).ifPresent(instancia -> {
                            boolean yaEsta = instancia.getJugadoresDisponibles().stream()
                .anyMatch(j -> j.getId().equals(jugador.getId()));
            if (!yaEsta) {
                instancia.getJugadoresDisponibles().add(jugador);
                instanciaRepository.save(instancia);
            }
            });
        }
    }

    // ─── Retirar jugador del mercado ──────────────────────────────────────────
    public void retirarDeVenta(Integer jugadorFantasyId, Integer equipoId) {
        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        if (!jugador.getEquipoFantasy().getId().equals(equipoId)) {
            throw new RuntimeException("Este jugador no pertenece a tu equipo");
        }

        jugador.setEnVenta(false);
        jugadorFantasyRepository.save(jugador);

        // Quitar de la instancia activa
        LigaFantasy liga = jugador.getLigaFantasy();
        Mercado mercado  = mercadoRepository.findByLigaFantasyId(liga.getId()).orElse(null);
        if (mercado != null) {
            instanciaRepository.findByMercadoAndResueltaFalse(mercado).ifPresent(instancia -> {
                instancia.getJugadoresDisponibles().removeIf(j -> j.getId().equals(jugador.getId()));
                instanciaRepository.save(instancia);    
            });
        }

        // Rechazar pujas pendientes sobre este jugador
        List<Puja> pendientes = pujaRepository.findByJugadorFantasyAndResueltaFalse(jugador);
        for (Puja p : pendientes) {
            p.setResuelta(true);
        }
        pujaRepository.saveAll(pendientes);
    }

    // ─── Aceptar puja (el vendedor acepta una oferta) ────────────────────────
    public void aceptarPuja(Integer pujaId, Integer userId) {
        Puja puja = pujaRepository.findById(pujaId)
            .orElseThrow(() -> new RuntimeException("Puja no encontrada"));

        JugadorFantasy jugador  = puja.getJugadorFantasy();
        EquipoFantasy vendedor  = jugador.getEquipoFantasy();
        
        if (!vendedor.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para aceptar esta oferta");
        }

        // Transferir dinero al vendedor
        vendedor.setDinero(vendedor.getDinero() + puja.getCantidad());

        if (puja.getEquipoComprador() != null) {
            // Oferta de otro usuario: descontar al comprador
            EquipoFantasy comprador = puja.getEquipoComprador();
            comprador.setDinero(comprador.getDinero() - puja.getCantidad());
            jugador.setEquipoFantasy(comprador);
            jugador.setClausula(puja.getCantidad());
            jugador.setClausulaBloqueadaHasta(LocalDateTime.now().plusDays(14));
            jugador.setFechaCompra(LocalDateTime.now());
            equipoFantasyRepository.save(comprador);
        } else {
            // Oferta del sistema: jugador queda libre
            jugador.setEquipoFantasy(null);
            jugador.setClausula(null);
            jugador.setClausulaBloqueadaHasta(null);
        }

        jugador.setEnVenta(false);
        jugador.setAlineado(false);
        puja.setAceptada(true);
        puja.setResuelta(true);

        // Rechazar el resto de pujas pendientes sobre este jugador
        List<Puja> otrasPujas = pujaRepository.findByJugadorFantasyAndResueltaFalse(jugador);
        for (Puja p : otrasPujas) {
            if (!p.getId().equals(pujaId)) {
                p.setResuelta(true);
            }
        }
        pujaRepository.saveAll(otrasPujas);

        // Quitar de la instancia activa
        LigaFantasy liga = jugador.getLigaFantasy();
        Mercado mercado  = mercadoRepository.findByLigaFantasyId(liga.getId()).orElse(null);
        if (mercado != null) {
            instanciaRepository.findByMercadoAndResueltaFalse(mercado).ifPresent(instancia -> {
                instancia.getJugadoresDisponibles().removeIf(j -> j.getId().equals(jugador.getId()));
                instanciaRepository.save(instancia);
            });
        }

        equipoFantasyRepository.save(vendedor);

        // Crear noticias
        String jugadorNombre = jugador.getJugadorReal().getFullName();

        if (puja.getEquipoComprador() != null) {
            // Venta entre usuarios
            String compradorNombre = puja.getEquipoComprador().getUser().getUsername();
            String vendedorNombre  = vendedor.getUser().getUsername();
            noticiaService.crearNoticiaCompra(liga, compradorNombre, jugadorNombre,
                vendedorNombre, puja.getCantidad());
        } else {
            // Venta al sistema
            String vendedorNombre = vendedor.getUser().getUsername();
            noticiaService.crearNoticiaVenta(liga, vendedorNombre, jugadorNombre,
                "La Liga", puja.getCantidad());
        }

        jugadorFantasyRepository.save(jugador);
    }

    // ─── Mis compras (pujas activas del comprador) ────────────────────────────
    public List<PujaResponse> obtenerMisPujas(Integer equipoId) {
        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        List<Puja> pujas = pujaRepository.findByEquipoCompradorAndResueltaFalse(equipo);
        List<PujaResponse> resultado = new ArrayList<>();
        for (Puja p : pujas) resultado.add(toPujaResponse(p));
        return resultado;
    }

    // ─── Mis ventas ───────────────────────────────────────────────────────────
    public List<VentaResponse> obtenerMisVentas(Integer equipoId) {
        List<JugadorFantasy> misJugadores = jugadorFantasyRepository
            .findByEquipoFantasyId(equipoId);

        List<VentaResponse> resultado = new ArrayList<>();

        for (JugadorFantasy jf : misJugadores) {
            List<Puja> pujasPendientes = pujaRepository
                .findByJugadorFantasyAndResueltaFalse(jf)
                .stream()
                .filter(p -> p.getEquipoVendedor() != null &&
                             p.getEquipoVendedor().getId().equals(equipoId))
                .toList();

            if (!jf.isEnVenta() && pujasPendientes.isEmpty()) continue;

            Jugador jr = jf.getJugadorReal();
            List<VentaResponse.OfertaRecibidaResponse> ofertas = pujasPendientes.stream()
                .map(p -> new VentaResponse.OfertaRecibidaResponse(
                    p.getId(),
                    p.getCantidad(),
                    p.getEquipoComprador() != null
                        ? p.getEquipoComprador().getUser().getUsername()
                        : "La Liga",
                    p.getFecha()
                )).toList();

            resultado.add(new VentaResponse(
                jf.getId(),
                jr.getFullName(),
                jr.getPosicion(),
                jr.getValorMercado(),
                jr.getEquipo() != null ? jr.getEquipo().getName() : null,
                jf.isEnVenta(),
                ofertas,jf.getClausula(),             
                jf.getClausulaBloqueadaHasta()
            ));
        }
        return resultado;
    }

    // ─── Editar puja ──────────────────────────────────────────────────────────
    public PujaResponse editarPuja(Integer pujaId, long nuevaCantidad, Integer userId) {
        Puja puja = pujaRepository.findById(pujaId)
            .orElseThrow(() -> new RuntimeException("Puja no encontrada"));
        if (puja.getEquipoComprador() == null ||
            !puja.getEquipoComprador().getUser().getId().equals(userId)) {
            throw new RuntimeException("Esta puja no es tuya");
        }
        validarSaldo(puja.getEquipoComprador(), puja.getInstancia(), nuevaCantidad, pujaId);
        puja.setCantidad(nuevaCantidad);
        pujaRepository.save(puja);
        return toPujaResponse(puja);
    }

    // ─── Eliminar puja ────────────────────────────────────────────────────────
    public void eliminarPuja(Integer pujaId, Integer userId) {
        Puja puja = pujaRepository.findById(pujaId)
            .orElseThrow(() -> new RuntimeException("Puja no encontrada"));
        if (puja.getEquipoComprador() == null ||
            !puja.getEquipoComprador().getUser().getId().equals(userId)) {
            throw new RuntimeException("Esta puja no es tuya");
        }
        pujaRepository.delete(puja);
    }

    // ─── Contadores de pujas por jugador ──────────────────────────────────────
    public Map<Integer, Long> obtenerContadorPujas(Integer ligaId) {
        Mercado mercado = mercadoRepository.findByLigaFantasyId(ligaId)
            .orElseThrow(() -> new RuntimeException("Mercado no encontrado"));
        InstanciaMercado instancia = instanciaRepository
            .findByMercadoAndResueltaFalse(mercado)
            .orElseThrow(() -> new RuntimeException("No hay instancia activa"));

        Map<Integer, Long> contadores = new HashMap<>();
        for (JugadorFantasy jf : instancia.getJugadoresDisponibles()) {
            long num = pujaRepository.findByJugadorFantasyAndInstancia(jf, instancia)
                .stream().filter(p -> !p.isResuelta()).count();
            if (num > 0) contadores.put(jf.getId(), num);
        }
        return contadores;
    }

    // ─── Resolver instancia ───────────────────────────────────────────────────
    private void resolverInstancia(InstanciaMercado instancia) {
        for (JugadorFantasy jugador : instancia.getJugadoresDisponibles()) {
            List<Puja> pujas = pujaRepository
                .findByJugadorFantasyAndInstancia(jugador, instancia)
                .stream().filter(p -> !p.isResuelta()).toList();

            if (pujas.isEmpty()) continue;

            // Puja ganadora: la más alta
            Puja ganadora = pujas.stream()
                .max((a, b) -> Long.compare(a.getCantidad(), b.getCantidad()))
                .orElse(null);

            if (ganadora == null) continue;

            EquipoFantasy vendedor   = jugador.getEquipoFantasy();
            EquipoFantasy comprador  = ganadora.getEquipoComprador();

            if (vendedor != null) {
                vendedor.setDinero(vendedor.getDinero() + ganadora.getCantidad());
                equipoFantasyRepository.save(vendedor);
            }

            if (comprador != null) {
                comprador.setDinero(comprador.getDinero() - ganadora.getCantidad());
                jugador.setEquipoFantasy(comprador);
                jugador.setClausula(ganadora.getCantidad());
                jugador.setClausulaBloqueadaHasta(LocalDateTime.now().plusDays(14));
                jugador.setFechaCompra(LocalDateTime.now());
                equipoFantasyRepository.save(comprador);
            } else {
                jugador.setEquipoFantasy(null);
                jugador.setClausula(null);
                jugador.setClausulaBloqueadaHasta(null);
            }

            jugador.setEnVenta(false);
            jugador.setAlineado(false);
            ganadora.setAceptada(true);
            ganadora.setResuelta(true);
            jugadorFantasyRepository.save(jugador);
            pujaRepository.save(ganadora);

            // Crear noticias de compra/venta
            LigaFantasy liga = jugador.getLigaFantasy();
            String jugadorNombre = jugador.getJugadorReal().getFullName();
            if (comprador != null && vendedor != null) {
                noticiaService.crearNoticiaCompra(liga,
                    comprador.getUser().getUsername(), jugadorNombre,
                    vendedor.getUser().getUsername(), ganadora.getCantidad());
                noticiaService.crearNoticiaVenta(liga,
                    vendedor.getUser().getUsername(), jugadorNombre,
                    comprador.getUser().getUsername(), ganadora.getCantidad());
            } else if (comprador != null) {
                // Jugador sin equipo fichado
                noticiaService.crearNoticiaCompra(liga,
                    comprador.getUser().getUsername(), jugadorNombre,
                    "La Liga", ganadora.getCantidad());
            }

            // Rechazar el resto
            for (Puja p : pujas) {
                if (!p.getId().equals(ganadora.getId())) {
                    p.setResuelta(true);
                    pujaRepository.save(p);
                }
            }
        }

        // Generar ofertas del sistema para jugadores en venta
        generarOfertasSistema(instancia.getMercado().getLiga());

        instancia.setResuelta(true);
        instanciaRepository.save(instancia);
    }

    // ─── Generar oferta del sistema para todos los jugadores en venta ─────────
    private void generarOfertasSistema(LigaFantasy liga) {
        List<JugadorFantasy> enVenta = jugadorFantasyRepository
            .findByLigaFantasyIdAndEnVentaTrue(liga.getId());
        for (JugadorFantasy jf : enVenta) {
            if (jf.getEquipoFantasy() == null) continue;
            generarOfertaSistemaParaJugador(jf);
        }
    }

    private void generarOfertaSistemaParaJugador(JugadorFantasy jugador) {
        long valor   = jugador.getJugadorReal().getValorMercado();
        double factor = 0.9 + (new Random().nextDouble() * 0.2);
        long cantidad = (long)(valor * factor);
        Puja oferta   = new Puja(jugador.getEquipoFantasy(), jugador, cantidad);
        pujaRepository.save(oferta);
    }

    // ─── Crear nueva instancia ────────────────────────────────────────────────
    private void crearNuevaInstancia(InstanciaMercado anterior) {
        LigaFantasy liga = anterior.getMercado().getLiga();
        List<JugadorFantasy> nuevos = obtenerJugadoresParaMercado(liga);
        InstanciaMercado nueva = new InstanciaMercado(anterior.getMercado(), nuevos);
        instanciaRepository.save(nueva);
        programarResolucion(nueva);
    }

    // ─── Programar resolución automática ─────────────────────────────────────
    private void programarResolucion(InstanciaMercado instancia) {
        Instant fechaCierre = instancia.getFin()
            .atZone(ZoneId.systemDefault()).toInstant();
        Integer instanciaId = instancia.getId();
        taskScheduler.schedule(() -> {
            InstanciaMercado inst = instanciaRepository.findById(instanciaId).orElse(null);
            if (inst != null && !inst.isResuelta()) {
                resolverInstancia(inst);
                crearNuevaInstancia(inst);
            }
        }, fechaCierre);
    }

    public void rechazarPuja(Integer pujaId, Integer userId) {
        Puja puja = pujaRepository.findById(pujaId)
            .orElseThrow(() -> new RuntimeException("Puja no encontrada"));

        JugadorFantasy jugador = puja.getJugadorFantasy();
        EquipoFantasy vendedor = jugador.getEquipoFantasy();

        if (!vendedor.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para rechazar esta oferta");
        }

        puja.setAceptada(false);
        puja.setResuelta(true);
        pujaRepository.save(puja);

        // Notificar al comprador que su oferta fue rechazada
        if (puja.getEquipoComprador() != null) {
            LigaFantasy liga = puja.getJugadorFantasy().getLigaFantasy();
            String vendedorNombre  = vendedor.getUser().getUsername();
            String jugadorNombre   = puja.getJugadorFantasy().getJugadorReal().getFullName();
            Integer userIdComprador = puja.getEquipoComprador().getUser().getId();
            noticiaService.crearNoticiaOfertaRechazada(liga, userIdComprador,
                vendedorNombre, jugadorNombre);
        }
    }

    // ─── Jugadores disponibles para el mercado ────────────────────────────────
    private List<JugadorFantasy> obtenerJugadoresParaMercado(LigaFantasy liga) {
        List<JugadorFantasy> sinEquipo = new ArrayList<>(jugadorFantasyRepository
            .findByLigaFantasyIdAndEquipoFantasyIsNull(liga.getId()));
        List<JugadorFantasy> enVenta = jugadorFantasyRepository
            .findByLigaFantasyIdAndEnVentaTrue(liga.getId());

        Collections.shuffle(sinEquipo);
        List<JugadorFantasy> resultado = new ArrayList<>(
            sinEquipo.subList(0, Math.min(5, sinEquipo.size()))
        );
        for (JugadorFantasy jf : enVenta) {
            if (!resultado.contains(jf)) resultado.add(jf);
        }
        return resultado;
    }

    // ─── Validar saldo ────────────────────────────────────────────────────────
    private void validarSaldo(EquipoFantasy comprador, InstanciaMercado instancia,
            long cantidad, Integer pujaIdExcluir) {
        long valorEquipo  = jugadorFantasyRepository.findByEquipoFantasyId(comprador.getId())
            .stream().mapToLong(j -> j.getJugadorReal().getValorMercado()).sum();
        long maxNegativo  = -(valorEquipo / 5);

        long pujasTotales = 0;
        if (instancia != null) {
            pujasTotales = pujaRepository.findByEquipoCompradorAndInstancia(comprador, instancia)
                .stream()
                .filter(p -> !p.isResuelta() && (pujaIdExcluir == null || !p.getId().equals(pujaIdExcluir)))
                .mapToLong(Puja::getCantidad).sum();
        }

        long saldoTras = comprador.getDinero() - pujasTotales - cantidad;
        if (saldoTras < maxNegativo) {
            long maxPuja = comprador.getDinero() - pujasTotales - maxNegativo;
            throw new RuntimeException("Puedes pujar como máximo " + formatearDinero(Math.max(0, maxPuja)));
        }
    }

    // ─── Conversores ─────────────────────────────────────────────────────────
    private InstanciaMercadoResponse toInstanciaResponse(InstanciaMercado instancia) {
        List<JugadorFantasyDetalleResponse> jugadores = new ArrayList<>();
        for (JugadorFantasy jf : instancia.getJugadoresDisponibles()) {
            var jr = jf.getJugadorReal();
            int partidos = jr.getPartidosJugados();
            double media = partidos > 0 ? (double) jr.getPuntosFantasy() / partidos : 0.0;
            String dueno = (jf.getEquipoFantasy() != null && jf.isEnVenta())
                ? jf.getEquipoFantasy().getUser().getUsername() : null;
            Integer userIdDueno = (jf.getEquipoFantasy() != null && jf.isEnVenta())
                ? jf.getEquipoFantasy().getUser().getId() : null;
            jugadores.add(new JugadorFantasyDetalleResponse(
                jf.getId(), jr.getId(), jr.getFullName(), jr.getPosicion(),
                jr.getValorMercado(),
                jr.getEquipo() != null ? jr.getEquipo().getName() : null,
                jr.getPuntosFantasy(), media, partidos,
                jf.getClausula(), jf.getClausulaBloqueadaHasta(), jf.isAlineado(),
                dueno, userIdDueno, jf.isEnVenta()
            ));
        }
        return new InstanciaMercadoResponse(instancia.getId(), instancia.getFin(), jugadores);
    }

    private PujaResponse toPujaResponse(Puja p) {
        var jr = p.getJugadorFantasy().getJugadorReal();
        String duenoNombre = p.getEquipoVendedor() != null
            ? p.getEquipoVendedor().getUser().getUsername() : null;
        String ofertante = p.getEquipoComprador() != null
            ? p.getEquipoComprador().getUser().getUsername() : "La Liga";
        return new PujaResponse(
            p.getId(), p.getJugadorFantasy().getId(), jr.getFullName(),
            jr.getPosicion(), jr.getValorMercado(),
            jr.getEquipo() != null ? jr.getEquipo().getName() : null,
            p.getCantidad(), p.getFecha(), duenoNombre, ofertante
        );
    }

    private String formatearDinero(long cantidad) {
        if (cantidad >= 1000000) return (cantidad / 1000000) + "M";
        if (cantidad >= 1000)    return (cantidad / 1000) + "K";
        return String.valueOf(cantidad);
    }
}