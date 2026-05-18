package com.ligainternaetsiinf.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.NoticiaResponse;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.model.NoticiaFantasy;
import com.ligainternaetsiinf.repository.NoticiaFantasyRepository;

@Service
public class NoticiaFantasyService {

    @Autowired
    private NoticiaFantasyRepository noticiaRepository;

    public List<NoticiaResponse> obtenerNoticias(Integer ligaId, Integer userId) {
        return noticiaRepository.findNoticiasVisibles(ligaId, userId)
            .stream()
            .map(n -> new NoticiaResponse(
                n.getId(), n.getTitulo(), n.getNoticia(),
                n.getFecha(), n.getHora(),
                n.getUserIdDestinatario() != null
            ))
            .collect(Collectors.toList());
    }

    // ─── Noticias públicas ────────────────────────────────────────────────────

    public void crearNoticiaCompra(LigaFantasy liga, String compradorNombre,
            String jugadorNombre, String vendedorNombre, long cantidad) {
        String texto = compradorNombre + " ha comprado al jugador " + jugadorNombre
            + " a " + vendedorNombre + " por " + formatearDinero(cantidad) + " €";
        noticiaRepository.save(new NoticiaFantasy(liga, "COMPRA", texto));
    }

    public void crearNoticiaVenta(LigaFantasy liga, String vendedorNombre,
            String jugadorNombre, String compradorNombre, long cantidad) {
        String texto = vendedorNombre + " ha vendido al jugador " + jugadorNombre
            + " a " + compradorNombre + " por " + formatearDinero(cantidad) + " €";
        noticiaRepository.save(new NoticiaFantasy(liga, "VENTA", texto));
    }

    public void crearNoticiaNoPuntuacion(LigaFantasy liga, String usuarioNombre,
            Integer jornada, String motivo) {
        String texto = usuarioNombre + " no puntúa en la jornada " + jornada
            + " por " + motivo;
        noticiaRepository.save(new NoticiaFantasy(liga, "SIN PUNTUACIÓN", texto));
    }

    // ─── Noticias privadas ────────────────────────────────────────────────────

    public void crearNoticiaOfertaRecibida(LigaFantasy liga, Integer userIdVendedor,
            String compradorNombre, String jugadorNombre) {
        String texto = compradorNombre + " te ha hecho una oferta por el jugador " + jugadorNombre;
        noticiaRepository.save(
            new NoticiaFantasy(liga, "OFERTA RECIBIDA", texto, userIdVendedor));
    }

    public void crearNoticiaOfertaRechazada(LigaFantasy liga, Integer userIdComprador,
            String vendedorNombre, String jugadorNombre) {
        String texto = vendedorNombre + " ha rechazado tu oferta por el jugador " + jugadorNombre;
        noticiaRepository.save(
            new NoticiaFantasy(liga, "RECHAZADA", texto, userIdComprador));
    }

    // ─── Utilidad ─────────────────────────────────────────────────────────────
    private String formatearDinero(long cantidad) {
        if (cantidad >= 1000000) {
            long millones = cantidad / 1000000;
            long resto    = (cantidad % 1000000) / 1000;
            return resto > 0 ? millones + "." + String.format("%03d", resto) + ".000"
                             : millones + ".000.000";
        }
        if (cantidad >= 1000) return (cantidad / 1000) + ".000";
        return String.valueOf(cantidad);
    }
}