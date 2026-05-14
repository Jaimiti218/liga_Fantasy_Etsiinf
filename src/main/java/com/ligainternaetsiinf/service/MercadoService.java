package com.ligainternaetsiinf.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.InstanciaMercadoResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.OfertaVentaResponse;
import com.ligainternaetsiinf.dto.PujaResponse;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.InstanciaMercado;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.model.Mercado;
import com.ligainternaetsiinf.model.OfertaVenta;
import com.ligainternaetsiinf.model.Puja;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;
import com.ligainternaetsiinf.repository.InstanciaMercadoRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.MercadoRepository;
import com.ligainternaetsiinf.repository.OfertaVentaRepository;
import com.ligainternaetsiinf.repository.PujaRepository;
import java.time.Instant;
import java.time.ZoneId;

@Service
public class MercadoService {

    @Autowired private MercadoRepository mercadoRepository;
    @Autowired private InstanciaMercadoRepository instanciaRepository;
    @Autowired private JugadorFantasyRepository jugadorFantasyRepository;
    @Autowired private EquipoFantasyRepository equipoFantasyRepository;
    @Autowired private PujaRepository pujaRepository;
    @Autowired private OfertaVentaRepository ofertaVentaRepository;
    @Autowired
    private TaskScheduler taskScheduler;

    // ─── Crear primera instancia al crear la liga ─────────────────────────────
    public void crearPrimeraInstancia(Mercado mercado) {
        List<JugadorFantasy> disponibles = obtenerJugadoresParaMercado(mercado.getLiga());
        InstanciaMercado instancia = new InstanciaMercado(mercado, disponibles);
        instanciaRepository.save(instancia);
        programarResolucion(instancia);
    }

    // ─── Obtener instancia activa de una liga ─────────────────────────────────
    public InstanciaMercadoResponse obtenerInstanciaActiva(Integer ligaId) {
        Mercado mercado = mercadoRepository.findByLigaFantasyId(ligaId)
            .orElseThrow(() -> new RuntimeException("Mercado no encontrado"));

        InstanciaMercado instancia = instanciaRepository
            .findByMercadoAndResueltaFalse(mercado)
            .orElseThrow(() -> new RuntimeException("No hay instancia de mercado activa"));

        return toInstanciaResponse(instancia);
    }

    // ─── Realizar puja ────────────────────────────────────────────────────────
    public PujaResponse realizarPuja(Integer equipoId, Integer jugadorFantasyId,
            Integer instanciaId, long cantidad) {

        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorFantasyId)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        InstanciaMercado instancia = instanciaRepository.findById(instanciaId)
            .orElseThrow(() -> new RuntimeException("Instancia no encontrada"));

        

        // Verificar límite de saldo negativo (máximo 20% del valor del equipo)
        long valorEquipo = calcularValorEquipo(equipo);
        long maxNegativo = -(valorEquipo / 5); // 20%
        long pujasTotales = calcularPujasTotales(equipo, instancia);
        long saldoTrasLaPuja = equipo.getDinero() - pujasTotales - cantidad;

        if (saldoTrasLaPuja < maxNegativo) {
            long maxPuja = equipo.getDinero() - pujasTotales - maxNegativo;
            throw new RuntimeException(
                "No puedes realizar esta puja. Tu saldo máximo en negativo es " +
                formatearDinero(Math.abs(maxNegativo)) +
                ". Puedes pujar como máximo " + formatearDinero(Math.max(0, maxPuja))
            );
        }

        // Si ya había una puja de este equipo por este jugador, actualizarla
        Optional<Puja> pujaExistente = pujaRepository
            .findByEquipoFantasyAndJugadorFantasyAndInstancia(equipo, jugador, instancia);

        Puja puja;
        if (pujaExistente.isPresent()) {
            puja = pujaExistente.get();
            puja.setCantidad(cantidad);
        } else {
            puja = new Puja(equipo, jugador, instancia, cantidad);
        }
        pujaRepository.save(puja);

        return toPujaResponse(puja);
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

        // Añadir a la instancia activa si existe
        LigaFantasy liga = jugador.getLigaFantasy();
        Mercado mercado  = mercadoRepository.findByLigaFantasyId(liga.getId()).orElse(null);
        if (mercado != null) {
            instanciaRepository.findByMercadoAndResueltaFalse(mercado).ifPresent(instancia -> {
                if (!instancia.getJugadoresDisponibles().contains(jugador)) {
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
                instancia.getJugadoresDisponibles().remove(jugador);
                instanciaRepository.save(instancia);
            });
        }
    }

    // ─── Aceptar oferta de venta ──────────────────────────────────────────────
    public void aceptarOfertaVenta(Integer ofertaId, Integer equipoId) {
        OfertaVenta oferta = ofertaVentaRepository.findById(ofertaId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        if (!oferta.getEquipoVendedor().getId().equals(equipoId)) {
            throw new RuntimeException("Esta oferta no es tuya");
        }

        JugadorFantasy jugador = oferta.getJugadorFantasy();
        EquipoFantasy vendedor  = oferta.getEquipoVendedor();

        // Transferir dinero al vendedor
        vendedor.setDinero(vendedor.getDinero() + oferta.getCantidad());
        equipoFantasyRepository.save(vendedor);

        if (oferta.getEquipoComprador() != null) {
            // Venta entre usuarios: el jugador pasa al comprador
            EquipoFantasy comprador = oferta.getEquipoComprador();
            comprador.setDinero(comprador.getDinero() - oferta.getCantidad());
            jugador.setEquipoFantasy(comprador);
            jugador.setClausula(oferta.getCantidad()); // clausula = precio de compra
            jugador.setClausulaBloqueadaHasta(LocalDateTime.now().plusDays(14));
            jugador.setFechaCompra(LocalDateTime.now());
            equipoFantasyRepository.save(comprador);
        } else {
            // Venta al sistema: el jugador queda libre
            jugador.setEquipoFantasy(null);
            jugador.setClausula(null);
            jugador.setClausulaBloqueadaHasta(null);
        }

        jugador.setEnVenta(false);
        jugador.setAlineado(false);
        oferta.setAceptada(true);

        jugadorFantasyRepository.save(jugador);

        LigaFantasy liga = jugador.getLigaFantasy();
        Mercado mercado  = mercadoRepository.findByLigaFantasyId(liga.getId()).orElse(null);
        if (mercado != null) {
            instanciaRepository.findByMercadoAndResueltaFalse(mercado).ifPresent(instancia -> {
                instancia.getJugadoresDisponibles().remove(jugador);
                instanciaRepository.save(instancia);
            });
        }
        ofertaVentaRepository.save(oferta);
    }

    public void aceptarOfertaVentaPorUsuario(Integer ofertaId, Integer userId) {
        OfertaVenta oferta = ofertaVentaRepository.findById(ofertaId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));
        if (!oferta.getEquipoVendedor().getUser().getId().equals(userId)) {
            throw new RuntimeException("Esta oferta no es tuya");
        }
        aceptarOfertaVenta(ofertaId, oferta.getEquipoVendedor().getId());
    }

    // ─── Obtener mis pujas ────────────────────────────────────────────────────
    public List<PujaResponse> obtenerMisPujas(Integer equipoId, Integer ligaId) {
        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Mercado mercado = mercadoRepository.findByLigaFantasyId(ligaId)
            .orElseThrow(() -> new RuntimeException("Mercado no encontrado"));

        InstanciaMercado instancia = instanciaRepository
            .findByMercadoAndResueltaFalse(mercado)
            .orElseThrow(() -> new RuntimeException("No hay instancia activa"));

        List<Puja> pujas = pujaRepository.findByEquipoFantasyAndInstancia(equipo, instancia);
        List<PujaResponse> resultado = new ArrayList<>();
        for (Puja p : pujas) resultado.add(toPujaResponse(p));
        return resultado;
    }

    public void eliminarPuja(Integer pujaId, Integer userId) {
        Puja puja = pujaRepository.findById(pujaId)
            .orElseThrow(() -> new RuntimeException("Puja no encontrada"));
        if (!puja.getEquipoFantasy().getUser().getId().equals(userId)) {
            throw new RuntimeException("Esta puja no es tuya");
        }
        pujaRepository.delete(puja);
    }

    public PujaResponse editarPuja(Integer pujaId, long nuevaCantidad, Integer userId) {
        Puja puja = pujaRepository.findById(pujaId)
            .orElseThrow(() -> new RuntimeException("Puja no encontrada"));
        if (!puja.getEquipoFantasy().getUser().getId().equals(userId)) {
            throw new RuntimeException("Esta puja no es tuya");
        }

        // Validar límite de saldo negativo
        EquipoFantasy equipo  = puja.getEquipoFantasy();
        long valorEquipo      = calcularValorEquipo(equipo);
        long maxNegativo      = -(valorEquipo / 5);
        long pujasTotales     = calcularPujasTotales(equipo, puja.getInstancia()) - puja.getCantidad();
        long saldoTras        = equipo.getDinero() - pujasTotales - nuevaCantidad;

        if (saldoTras < maxNegativo) {
            long maxPuja = equipo.getDinero() - pujasTotales - maxNegativo;
            throw new RuntimeException("Puedes pujar como máximo " + formatearDinero(Math.max(0, maxPuja)));
        }

        puja.setCantidad(nuevaCantidad);
        pujaRepository.save(puja);
        return toPujaResponse(puja);
    }

    // ─── Obtener mis ventas ───────────────────────────────────────────────────
    public List<JugadorFantasyDetalleResponse> obtenerMisVentas(Integer equipoId) {
        List<JugadorFantasy> enVenta = jugadorFantasyRepository
            .findByEquipoFantasyIdAndEnVentaTrue(equipoId);
        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        List<JugadorFantasyDetalleResponse> resultado = new ArrayList<>();
        for (JugadorFantasy jf : enVenta) {
            Jugador jr = jf.getJugadorReal();
            int partidos = jr.getPartidosJugados();
            double media = partidos > 0 ? (double) jr.getPuntosFantasy() / partidos : 0.0;
            resultado.add(new JugadorFantasyDetalleResponse(
                jf.getId(), jr.getId(), jr.getFullName(), jr.getPosicion(),
                jr.getValorMercado(),
                jr.getEquipo() != null ? jr.getEquipo().getName() : null,
                jr.getPuntosFantasy(), media, partidos,
                jf.getClausula(), jf.getClausulaBloqueadaHasta(), jf.isAlineado(), null, 
                equipo.getUser().getId(), jf.isEnVenta()
            ));
        }
        return resultado;
    }

    private void programarResolucion(InstanciaMercado instancia) {
        Instant fechaCierre = instancia.getFin()
            .atZone(ZoneId.systemDefault())
            .toInstant();

        Integer instanciaId = instancia.getId();

        taskScheduler.schedule(() -> {
            InstanciaMercado inst = instanciaRepository.findById(instanciaId)
                .orElse(null);
            if (inst != null && !inst.isResuelta()) {
                resolverInstancia(inst);
                crearNuevaInstancia(inst);
            }
        }, fechaCierre);
    }
    

    // ─── Resolver una instancia vencida ──────────────────────────────────────
    private void resolverInstancia(InstanciaMercado instancia) {
        List<JugadorFantasy> jugadores = instancia.getJugadoresDisponibles();

        for (JugadorFantasy jugador : jugadores) {
            List<Puja> pujas = pujaRepository
                .findByJugadorFantasyAndInstancia(jugador, instancia);

            if (pujas.isEmpty()) continue;

            // Encontrar la puja más alta
            Puja ganadora = pujas.stream()
                .max((a, b) -> Long.compare(a.getCantidad(), b.getCantidad()))
                .orElse(null);

            if (ganadora == null) continue;

            EquipoFantasy comprador = ganadora.getEquipoFantasy();
            EquipoFantasy vendedorAnterior = jugador.getEquipoFantasy();

            // Transferir jugador
            if (vendedorAnterior != null) {
                vendedorAnterior.setDinero(vendedorAnterior.getDinero() + ganadora.getCantidad());
                equipoFantasyRepository.save(vendedorAnterior);
            }

            comprador.setDinero(comprador.getDinero() - ganadora.getCantidad());
            jugador.setEquipoFantasy(comprador);
            jugador.setEnVenta(false);
            jugador.setAlineado(false);
            jugador.setClausula(ganadora.getCantidad());
            jugador.setClausulaBloqueadaHasta(LocalDateTime.now().plusDays(14));
            jugador.setFechaCompra(LocalDateTime.now());

            ganadora.setGanadora(true);

            equipoFantasyRepository.save(comprador);
            jugadorFantasyRepository.save(jugador);
            pujaRepository.save(ganadora);
        }

        // Generar ofertas del sistema por jugadores en venta
        generarOfertasSistema(instancia.getMercado().getLiga());

        instancia.setResuelta(true);
        instanciaRepository.save(instancia);
    }

    // ─── Generar ofertas del sistema ──────────────────────────────────────────
    private void generarOfertasSistema(LigaFantasy liga) {
        List<JugadorFantasy> enVenta = jugadorFantasyRepository
            .findByLigaFantasyIdAndEnVentaTrue(liga.getId());

        Random random = new Random();

        for (JugadorFantasy jugador : enVenta) {
            if (jugador.getEquipoFantasy() == null) continue;

            long valor = jugador.getJugadorReal().getValorMercado();
            double factor = 0.9 + (random.nextDouble() * 0.2); // entre 0.9 y 1.1
            long ofertaCantidad = (long)(valor * factor);

            OfertaVenta oferta = new OfertaVenta(
                jugador, jugador.getEquipoFantasy(), null, ofertaCantidad
            );
            ofertaVentaRepository.save(oferta);
        }
    }

    // ─── Crear nueva instancia ────────────────────────────────────────────────
    private void crearNuevaInstancia(InstanciaMercado anterior) {
        LigaFantasy liga = anterior.getMercado().getLiga();
        List<JugadorFantasy> nuevosJugadores = obtenerJugadoresParaMercado(liga);
        InstanciaMercado nueva = new InstanciaMercado(anterior.getMercado(), nuevosJugadores);
        instanciaRepository.save(nueva);
        programarResolucion(nueva);
    }

    // ─── Seleccionar 5 jugadores para el mercado ──────────────────────────────
    private List<JugadorFantasy> obtenerJugadoresParaMercado(LigaFantasy liga) {
        List<JugadorFantasy> sinEquipo = new ArrayList<>(jugadorFantasyRepository
            .findByLigaFantasyIdAndEquipoFantasyIsNull(liga.getId()));
        List<JugadorFantasy> enVenta = jugadorFantasyRepository
            .findByLigaFantasyIdAndEnVentaTrue(liga.getId());

        // Seleccionar 5 aleatorios de los sin equipo
        Collections.shuffle(sinEquipo);
        List<JugadorFantasy> resultado = new ArrayList<>(
            sinEquipo.subList(0, Math.min(5, sinEquipo.size()))
        );

        // Añadir todos los que están en venta (sin duplicados)
        for (JugadorFantasy jf : enVenta) {
            if (!resultado.contains(jf)) resultado.add(jf);
        }

        return resultado;
    }

    // ─── Calcular valor total del equipo ──────────────────────────────────────
    private long calcularValorEquipo(EquipoFantasy equipo) {
        return jugadorFantasyRepository.findByEquipoFantasyId(equipo.getId())
            .stream()
            .mapToLong(j -> j.getJugadorReal().getValorMercado())
            .sum();
    }

    // ─── Calcular total de pujas activas del equipo ───────────────────────────
    private long calcularPujasTotales(EquipoFantasy equipo, InstanciaMercado instancia) {
        return pujaRepository.findByEquipoFantasyAndInstancia(equipo, instancia)
            .stream()
            .mapToLong(Puja::getCantidad)
            .sum();
    }

    // ─── Conversores ─────────────────────────────────────────────────────────
    private InstanciaMercadoResponse toInstanciaResponse(InstanciaMercado instancia) {
        List<JugadorFantasyDetalleResponse> jugadores = new ArrayList<>();
        for (JugadorFantasy jf : instancia.getJugadoresDisponibles()) {
            var jr = jf.getJugadorReal();
            String dueno = (jf.getEquipoFantasy() != null && jf.isEnVenta())
            ? jf.getEquipoFantasy().getUser().getUsername()
            : null;
            Integer userIdDueno = (jf.getEquipoFantasy() != null && jf.isEnVenta())
            ? jf.getEquipoFantasy().getUser().getId()
            : null;
            int partidos = jr.getPartidosJugados();
            double media = partidos > 0 ? (double) jr.getPuntosFantasy() / partidos : 0.0;
            jugadores.add(new JugadorFantasyDetalleResponse(
                jf.getId(), jr.getId(), jr.getFullName(), jr.getPosicion(),
                jr.getValorMercado(),
                jr.getEquipo() != null ? jr.getEquipo().getName() : null,
                jr.getPuntosFantasy(), media, partidos,
                jf.getClausula(), jf.getClausulaBloqueadaHasta(), jf.isAlineado(), dueno, userIdDueno,
                jf.isEnVenta()
            ));
        }
        return new InstanciaMercadoResponse(instancia.getId(), instancia.getFin(), jugadores);
    }

    private PujaResponse toPujaResponse(Puja p) {
        var jr = p.getJugadorFantasy().getJugadorReal();
        return new PujaResponse(
            p.getId(), p.getJugadorFantasy().getId(), jr.getFullName(),
            jr.getPosicion(), jr.getValorMercado(),
            jr.getEquipo() != null ? jr.getEquipo().getName() : null,
            p.getCantidad(), p.getFecha()
        );
    }

    private OfertaVentaResponse toOfertaResponse(OfertaVenta o) {
        var jr = o.getJugadorFantasy().getJugadorReal();
        return new OfertaVentaResponse(
            o.getId(), o.getJugadorFantasy().getId(), jr.getFullName(),
            jr.getPosicion(), jr.getValorMercado(),
            jr.getEquipo() != null ? jr.getEquipo().getName() : null,
            o.getCantidad(), o.getFecha()
        );
    }

    private String formatearDinero(long cantidad) {
        if (cantidad >= 1000000) return (cantidad / 1000000) + "M";
        if (cantidad >= 1000) return (cantidad / 1000) + "K";
        return String.valueOf(cantidad);
    }

    public Map<Integer, Long> obtenerContadorPujas(Integer ligaId) {
        Mercado mercado = mercadoRepository.findByLigaFantasyId(ligaId)
            .orElseThrow(() -> new RuntimeException("Mercado no encontrado"));
        InstanciaMercado instancia = instanciaRepository
            .findByMercadoAndResueltaFalse(mercado)
            .orElseThrow(() -> new RuntimeException("No hay instancia activa"));

        List<JugadorFantasy> jugadores = instancia.getJugadoresDisponibles();
        Map<Integer, Long> contadores = new HashMap<>();
        for (JugadorFantasy jf : jugadores) {
            long numPujas = pujaRepository
                .findByJugadorFantasyAndInstancia(jf, instancia).size();
            if (numPujas > 0) contadores.put(jf.getId(), numPujas);
        }
        return contadores;
    }
}