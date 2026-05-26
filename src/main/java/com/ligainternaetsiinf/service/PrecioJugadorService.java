package com.ligainternaetsiinf.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ligainternaetsiinf.dto.VariacionPrecioResponse;
import com.ligainternaetsiinf.model.HistorialPrecioJugador;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.Puja;
import com.ligainternaetsiinf.repository.HistorialPrecioRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.JugadorRepository;
import com.ligainternaetsiinf.repository.PujaRepository;

@Service
public class PrecioJugadorService {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private JugadorFantasyRepository jugadorFantasyRepository;
    @Autowired private PujaRepository pujaRepository;
    @Autowired private HistorialPrecioRepository historialRepository;

    // ─── Actualización diaria a las 23:30 ────────────────────────────────────
    @Scheduled(cron = "0 19 22 * * *")
    @Transactional
    public void actualizarPreciosDiarios() {
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
        List<Jugador> jugadores = jugadorRepository.findAll();

        for (Jugador jugador : jugadores) {
            long precioActual = jugador.getValorMercado();

            // Guardar precio en historial ANTES de modificar
            historialRepository.save(new HistorialPrecioJugador(jugador, precioActual));

            // Calcular índice
            double indice = calcularIndice(jugador, hace24h);
            if (indice == 0.0) continue;

            // Nuevo precio con máximo ±10%
            double variacion = indice * 0.10;
            long nuevoPrecio = Math.round(precioActual * (1 + variacion));
            nuevoPrecio = Math.max(nuevoPrecio, 500_000L); // precio mínimo

            jugador.setValorMercado(nuevoPrecio);
            jugadorRepository.save(jugador);

            // Si subió, actualizar cláusulas que queden por debajo del nuevo precio
            if (nuevoPrecio > precioActual) {
                actualizarClausulasJugador(jugador, nuevoPrecio);
            }
        }
    }

    // ─── Cálculo del índice de demanda ────────────────────────────────────────
    private double calcularIndice(Jugador jugador, LocalDateTime desde) {
        Integer id = jugador.getId();

        long comprasAlSistema  = pujaRepository.contarComprasAlSistema(id, desde);
        long ventasAlSistema   = pujaRepository.contarVentasAlSistema(id, desde);
        long pujasActivas      = pujaRepository.contarPujasActivas(id, desde);
        long puestasEnVenta    = pujaRepository.contarPuestasEnVenta(id, desde);
        List<Puja> clausulazos = pujaRepository.findClausulazos(id, desde);

        // Peso de clausulazos: 3 base × ratio sobreprecio (cap en 2x)
        double pesoClausulazos = 0;
        long valorMercado = jugador.getValorMercado();
        for (Puja c : clausulazos) {
            if (c.getValorClausulaMomento() != null && valorMercado > 0) {
                double ratio = (double) c.getValorClausulaMomento() / valorMercado;
                pesoClausulazos += 3.0 * Math.min(ratio, 2.0);
            } else {
                pesoClausulazos += 3.0;
            }
        }

        // Presión por rendimiento (últimas 3 jornadas aproximadas por media)
        double presionRendimiento = calcularPresionRendimiento(jugador);

        double presionCompra = (comprasAlSistema * 3.0)
                             + (pujasActivas * 2.0)
                             + pesoClausulazos
                             + presionRendimiento;

        double presionVenta  = (ventasAlSistema * 3.0)
                             + (puestasEnVenta * 1.0);

        double actividadTotal = presionCompra + presionVenta;
        if (actividadTotal == 0) return 0.0;

        return (presionCompra - presionVenta) / actividadTotal;
    }

    // ─── Rendimiento: media de puntos normalizada ─────────────────────────────
    private double calcularPresionRendimiento(Jugador jugador) {
        if (jugador.getPartidosJugados() == 0) return 0.0;
        double media = (double) jugador.getPuntosFantasy() / jugador.getPartidosJugados();
        // 8 puntos de media = 1 unidad de presión positiva (cap en 1)
        return Math.min(media / 8.0, 1.0);
    }

    // ─── Actualizar cláusulas cuando el precio sube ───────────────────────────
    private void actualizarClausulasJugador(Jugador jugador, long nuevoPrecio) {
        List<JugadorFantasy> copias = jugadorFantasyRepository
            .findByJugadorRealId(jugador.getId());
        for (JugadorFantasy jf : copias) {
            if (jf.getClausula() != null && jf.getClausula() < nuevoPrecio) {
                jf.setClausula(nuevoPrecio);
                jugadorFantasyRepository.save(jf);
            }
        }
    }

    // ─── Obtener variaciones para la pantalla ─────────────────────────────────
    public List<VariacionPrecioResponse> obtenerVariaciones(String periodo, boolean subidas) {
        LocalDate fechaReferencia = switch (periodo) {
            case "dia" -> LocalDate.now().minusDays(1);
            case "mes" -> LocalDate.now().minusDays(30);
            default    -> LocalDate.now().minusDays(7);
        };

        List<Jugador> jugadores = jugadorRepository.findAll();
        List<VariacionPrecioResponse> resultado = new ArrayList<>();

        for (Jugador j : jugadores) {
            Optional<HistorialPrecioJugador> historial =
                historialRepository.findFirstByJugadorAndFechaLessThanEqualOrderByFechaDesc(
                    j, fechaReferencia);

            if (historial.isEmpty()) continue;

            long precioAnterior = historial.get().getPrecio();
            long precioActual   = j.getValorMercado();
            long diferencia     = precioActual - precioAnterior;

            if (subidas && diferencia <= 0) continue;
            if (!subidas && diferencia >= 0) continue;

            resultado.add(new VariacionPrecioResponse(
                j.getId(), j.getFullName(), j.getPosicion(),
                j.getEquipo() != null ? j.getEquipo().getName() : null,
                precioAnterior, precioActual, diferencia
            ));
        }

        resultado.sort(Comparator.comparingLong(v -> -Math.abs(v.getDiferencia())));
        return resultado.subList(0, Math.min(20, resultado.size()));
    }
}