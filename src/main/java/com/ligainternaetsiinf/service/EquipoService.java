package com.ligainternaetsiinf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ligainternaetsiinf.dto.EquipoUpdateDTO;
import com.ligainternaetsiinf.dto.EquipoResponse;
import com.ligainternaetsiinf.dto.JugadorResponse;
import com.ligainternaetsiinf.model.Equipo;
import com.ligainternaetsiinf.model.Jugador;
import com.ligainternaetsiinf.repository.EquipoRepository;
import com.ligainternaetsiinf.repository.JugadorRepository;

@Service
public class EquipoService {
    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;


    public Equipo crearEquipo(Equipo equipo){ /*El motivo por el que no podemos usar este equipo en el equipoRepository.save es porque
        aqui, la lista de jugadores, no contiene a los objetos jugador real, contiene unos objetos jugador que se han creado a partir de lo 
        que se ha mandado en el frontend, que es solo el nombre. Es decir, son objetos Jugador con solo el atributo "nombre" relleno */
        int i=0;
        if(equipoRepository.findByName(equipo.getName()).isPresent()){
            throw new RuntimeException("Ya existe este equipo!");
        }

        
        List<Jugador> jugadores = new ArrayList<>();
        while(i < equipo.getJugadores().size()){ 
            Optional<Jugador> aux = jugadorRepository.findByFullName(equipo.getJugadores().get(i).getFullName());
            if(!aux.isPresent()){
                throw new RuntimeException("No se ha encontrado al jugador " + equipo.getJugadores().get(i).getFullName());
            }
            if(aux.get().getEquipo() != null){
                throw new RuntimeException("El jugador " + equipo.getJugadores().get(i).getFullName() + " ya pertenece al equipo " + aux.get().getEquipo().getName());
            }
            jugadores.add(aux.get());
            i++;
        }

        return equipoRepository.save(new Equipo(equipo.getName(), jugadores)); /*al crear un equipo de 0, no tiene sentido rellenar los demas
        campos aparte del nombre y los jugadores, ya que todo va a ser 0, asi que no hace falta usar DTO ni cambiarlos aqui*/

    }

    public List<EquipoResponse> listarEquipos(){
        List<Equipo> aux = equipoRepository.findAll();

        List<EquipoResponse> resultado = new ArrayList<>();

        for(Equipo equipo : aux){
            resultado.add(cambioTipoRespuesta(equipo));
        }

        return resultado;

    }


    public Equipo editarEquipo(Integer id, EquipoUpdateDTO equipoActualizado){
        int i=0;

        Optional<Equipo> aux = equipoRepository.findById(id);
        if(!aux.isPresent()){
            throw new RuntimeException("No se ha encontrado al equipo");
        }
        Equipo equipoAModificar = aux.get();
        List<Jugador> jugadores = new ArrayList<>();
        if(equipoActualizado.getJugadores() != null){
            while(i < equipoActualizado.getJugadores().size()){ 
                Optional<Jugador> aux2 = jugadorRepository.findByFullName(equipoActualizado.getJugadores().get(i));
                
                if(!aux2.isPresent()){
                    throw new RuntimeException("No se ha encontrado al jugador " + equipoActualizado.getJugadores().get(i));
                }

                Jugador jugador = aux2.get();

                if(jugador.getEquipo() != null && !jugador.getEquipo().getId().equals(id)){
                    throw new RuntimeException("El jugador " + equipoActualizado.getJugadores().get(i) + " ya pertenece al equipo " + jugador.getEquipo().getName());
                }
                if(!equipoAModificar.getJugadores().contains(jugador)){ /*esto es en caso de que ese jugador no estuviese en el equipo, porque si ya 
                    estaba no es necesario poner este codigo */
                    jugador.setEquipo(equipoAModificar);
                    jugadorRepository.save(jugador);

                    jugadores.add(jugador);
                }
                
                i++;
            }

            for(Jugador j : equipoAModificar.getJugadores()){
                if(!equipoActualizado.getJugadores().contains(j)){ /*esto es para cuando has eliminado a un jugador del equipo */
                    j.setEquipo(null);
                    jugadorRepository.save(j);
                }
            }
        }

        
        
        if(equipoActualizado.getJugadores() != null){
            equipoAModificar.setJugadores(jugadores);
        }
        if(equipoActualizado.getNombre() != null){
            equipoAModificar.setName(equipoActualizado.getNombre());
        }

        if(equipoActualizado.getPuntos() != null){
            equipoAModificar.setPuntos(equipoActualizado.getPuntos());
        }

        if(equipoActualizado.getGolesAFavor() != null){
            equipoAModificar.setGolesAFavor(equipoActualizado.getGolesAFavor());
        }

        if(equipoActualizado.getGolesEnContra() != null){
            equipoAModificar.setGolesEnContra(equipoActualizado.getGolesEnContra());
        }

        if(equipoActualizado.getDiferenciaDeGoles() != null){
            equipoAModificar.setDiferenciaDeGoles(equipoActualizado.getDiferenciaDeGoles());
        }
        
        return equipoRepository.save(equipoAModificar);
    }


    public void eliminarEquipo(Integer id){

        if(!equipoRepository.existsById(id)){
            throw new RuntimeException("Equipo no encontrado");
        }

        equipoRepository.deleteById(id);
    }

    private EquipoResponse cambioTipoRespuesta(Equipo equipo) {
        List<String> nombresJugadores = new ArrayList<>();

        if(equipo.getJugadores() != null && !equipo.getJugadores().isEmpty()){
            for(Jugador j : equipo.getJugadores()){
                nombresJugadores.add(j.getFullName());
            }
        }

        return new EquipoResponse(equipo.getId(), equipo.getName(), nombresJugadores);
    }
}
