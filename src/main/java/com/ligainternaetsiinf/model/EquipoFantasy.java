
package com.ligainternaetsiinf.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
public class EquipoFantasy { /*cada objeto fantasy team representa, para entenderlo, al usuario en cada una de 
                            sus ligas fantasy, ya que, como un mismo usuario puede tener varias ligas fantasy distintas, dentro 
                            de cada liga tiene que tener como un "sub-usuario", que sería este fantasy team */
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private LigaFantasy ligaFantasy; //la liga fantasy a la que pertenece

    @ManyToOne
    private User user; //el usuario dueño de este equipo en su liga fantasy correspondiente

    private long dinero;

    private String formacion; // "3-2-1", "2-3-1", "2-2-2", "2-1-3"

    @OneToMany(mappedBy = "equipoFantasy")
    private List<JugadorFantasy> jugadores; //PARA LA ASIGNACION INICIAL DE JUGADORES no se aun que hacer, ya que dependera
    //un poco desde donde se haga la logica detras de la asignación real, si desde esta entidad en el constructor (es lo que creo, llamando a otra funcion que
    //este tambien en esta entidad) u otra cosa

    private int puntos;

    public EquipoFantasy(){}

    public EquipoFantasy(LigaFantasy ligaFantasy, User user, List<JugadorFantasy> jugadores){
        this.ligaFantasy = ligaFantasy;
        this.user = user;
        this.jugadores = jugadores;
        this.dinero = 100000000;
        this.puntos = 0;
        this.formacion = "2-2-2";
    }

    public Integer getId(){ return id; }

    public LigaFantasy getLigaFantasy(){ return ligaFantasy; }

    public void setLigaFantasy(LigaFantasy ligaFantasy){ this.ligaFantasy = ligaFantasy; }

    public User getUser(){ return user; }

    public void setUser(User user){ this.user = user; }

    public long getDinero(){ return dinero; }

    public void setDinero(long dinero){ this.dinero = dinero; }

    public List<JugadorFantasy> getJugadores(){ return jugadores; }

    public void setJugadores(List<JugadorFantasy> jugadores){
        this.jugadores = jugadores;
    }

    public int getPuntos(){ return puntos; }

    public void setPuntos(int puntos){ this.puntos = puntos; }

    public String getFormacion(){ return formacion; }
    public void setFormacion(String formacion){ this.formacion = formacion; }
}
