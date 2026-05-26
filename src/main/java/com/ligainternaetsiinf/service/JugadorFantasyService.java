package com.ligainternaetsiinf.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.JugadorFantasyDetalleResponse;
import com.ligainternaetsiinf.dto.JugadorFantasyResponse;
import com.ligainternaetsiinf.model.EquipoFantasy;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.model.JugadorFantasy;
import com.ligainternaetsiinf.model.LigaFantasy;
import com.ligainternaetsiinf.model.Puja;
import com.ligainternaetsiinf.repository.EquipoFantasyRepository;
import com.ligainternaetsiinf.repository.JugadorFantasyRepository;
import com.ligainternaetsiinf.repository.JugadorRepository;
import com.ligainternaetsiinf.repository.LigaFantasyRepository;
import com.ligainternaetsiinf.repository.PujaRepository;

@Service
public class JugadorFantasyService {

    @Autowired
    private JugadorFantasyRepository jugadorFantasyRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private LigaFantasyRepository ligaFantasyRepository;

    @Autowired
    private EquipoFantasyRepository equipoFantasyRepository;

    @Autowired
    private PujaRepository pujaRepository;

    public void crearJugadorFantasy(Integer jugadorRealId){
        /*este metodo se va a usar cuando, a mitad de temporada, por ejemplo en la ventana de fichajes de invierno, cuando se pueden
        inscribir nuevos jugadores, se inscriban. Ya que cuando tu crear una ligafantasy, en el metodo crear del service ya se crean
        todos los jugadores fantasy correspondientes a todos los jugadores reales. ES DECIR, este metodo sera para añadir a los jugadores NUEVOS
        a TODAS las ligasfantasy. */
        Jugador jugadorReal = jugadorRepository.findById(jugadorRealId)
                .orElseThrow(() -> new RuntimeException("Jugador real no encontrado"));

        List<LigaFantasy> todasLasLigas = ligaFantasyRepository.findAll();

        for(LigaFantasy lf : todasLasLigas){
            JugadorFantasy jf = new JugadorFantasy(jugadorReal, lf);

            jugadorFantasyRepository.save(jf);
        }
    }


    public JugadorFantasyResponse obtenerJugadorFantasy(Integer jugadorId){

        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador fantasy no encontrado"));

        return cambioTipoRespuesta(jugador);
    }

    public List<JugadorFantasyDetalleResponse> obtenerTodosJugadoresDeLiga(Integer ligaId) {
        List<JugadorFantasy> jugadores = jugadorFantasyRepository.findByLigaFantasyId(ligaId);
        List<JugadorFantasyDetalleResponse> resultado = new ArrayList<>();
        for (JugadorFantasy jf : jugadores) {
            Jugador jr = jf.getJugadorReal();
            int partidosJugados = jr.getPartidosJugados();
            double media = partidosJugados > 0 ? (double) jr.getPuntosFantasy() / partidosJugados : 0.0;
            String nombreEquipo = jr.getEquipo() != null ? jr.getEquipo().getName() : null;
            String dueno = jf.getEquipoFantasy() != null
                ? jf.getEquipoFantasy().getUser().getUsername() : null;
            Integer userIdDueno = jf.getEquipoFantasy() != null
                ? jf.getEquipoFantasy().getUser().getId() : null;
            resultado.add(new JugadorFantasyDetalleResponse(
                jf.getId(), jr.getId(), jr.getFullName(), jr.getPosicion(),
                jr.getValorMercado(), nombreEquipo, jr.getPuntosFantasy(),
                media, partidosJugados, jf.getClausula(),
                jf.getClausulaBloqueadaHasta(), jf.isAlineado(),
                dueno, userIdDueno, jf.isEnVenta()
            ));
        }
        return resultado;
    }


    /*este metodo en principio sera util y se utilizará para el mercado, PERO IGUAL HAY QUE HACERLO EN EL SERVICE DE MERCADO, NO LO SE AUN */
    public List<JugadorFantasyResponse> obtenerJugadoresSinEquipoFantasy(Integer ligaId){

        List<JugadorFantasy> jugadores = jugadorFantasyRepository.findByLigaFantasyIdAndEquipoFantasyIsNull(ligaId);

        List<JugadorFantasyResponse> resultado = new ArrayList<>();

        for(JugadorFantasy jf : jugadores){
            resultado.add(cambioTipoRespuesta(jf));
        }

        return resultado;
    }


    public List<JugadorFantasyResponse> obtenerJugadoresDeEquipo(Integer equipoId){

        List<JugadorFantasy> jugadores = jugadorFantasyRepository.findByEquipoFantasyId(equipoId);

        List<JugadorFantasyResponse> resultado = new ArrayList<>();

        for(JugadorFantasy jf : jugadores){
            resultado.add(cambioTipoRespuesta(jf));
        }

        return resultado;
    }



    public JugadorFantasyResponse subirClausula(Integer jugadorId, long cantidadASubir, Integer equipoFantasyId){

        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador fantasy no encontrado"));

        EquipoFantasy equipo = equipoFantasyRepository.findById(equipoFantasyId)
                .orElseThrow(() -> new RuntimeException("Equipo fantasy no encontrado"));

        if(jugador.getEquipoFantasy() == null){
            throw new RuntimeException("El jugador no pertenece a ningún equipo");
        }
        
        if(jugador.getEquipoFantasy().getId() != equipo.getId()){
            throw new RuntimeException("El jugador ya no pertenece a este equipo");
        }

        jugador.setClausula(jugador.getClausula() + (cantidadASubir * 2));
        jugadorFantasyRepository.save(jugador);

        equipo.setDinero(equipo.getDinero() - cantidadASubir);
        equipoFantasyRepository.save(equipo);

        return cambioTipoRespuesta(jugador);
    }


    public JugadorFantasyResponse ejecutarClausulazo(Integer jugadorId, Integer equipoCompradorId){

        JugadorFantasy jugador = jugadorFantasyRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador fantasy no encontrado"));

        EquipoFantasy equipoComprador = equipoFantasyRepository.findById(equipoCompradorId)
                .orElseThrow(() -> new RuntimeException("Equipo comprador no encontrado"));

        EquipoFantasy equipoActual = jugador.getEquipoFantasy();

        if(equipoActual == null){
            throw new RuntimeException("El jugador no pertenece a ningún equipo");
        }
        if(LocalDateTime.now().isBefore(jugador.getClausulaBloqueadaHasta())){
            throw new RuntimeException("El jugador se encuentra bloqueado");
        }

        long valorClausula = jugador.getClausula();
        long valorJugador  = jugador.getJugadorReal().getValorMercado();

        // Registrar como puja de clausulazo
        Puja clausulazo = new Puja(equipoComprador, equipoActual, jugador, valorClausula);
        clausulazo.setEsClausulazo(true);
        clausulazo.setAceptada(true);
        clausulazo.setResuelta(true);
        clausulazo.setValorClausulaMomento(valorClausula);
        pujaRepository.save(clausulazo);

        // Transferir dinero
        equipoComprador.setDinero(equipoComprador.getDinero() - valorClausula);
        equipoActual.setDinero(equipoActual.getDinero() + valorClausula);
        equipoFantasyRepository.save(equipoComprador);
        equipoFantasyRepository.save(equipoActual);

        jugador.setEquipoFantasy(equipoComprador);
        jugador.setClausula(valorClausula);
        jugador.setClausulaBloqueadaHasta(LocalDateTime.now().plusDays(14));
        jugador.setFechaCompra(LocalDateTime.now());
        jugadorFantasyRepository.save(jugador);
        jugadorFantasyRepository.save(jugador);

        return cambioTipoRespuesta(jugador);
    }


    private JugadorFantasyResponse cambioTipoRespuesta(JugadorFantasy jf){ /*creamos este metodo ya que el cambio del tipo de dato lo hacemos mucho */

        Integer equipoId = null; /*los inicializamos a null porque si el jugador no los tiene asignados, es mejor que sean null */
        String dueno = null;

        if(jf.getEquipoFantasy() != null){
            equipoId = jf.getEquipoFantasy().getId();
            dueno = jf.getEquipoFantasy().getUser().getUsername();
        }

        return new JugadorFantasyResponse(jf.getId(), jf.getJugadorReal().getId(), jf.getJugadorReal().getFullName(), jf.getJugadorReal().getPosicion(),
        jf.getJugadorReal().getValorMercado(), jf.getLigaFantasy().getId(), jf.getLigaFantasy().getName(), equipoId, dueno, jf.getClausula());
    }
}
