package com.ligainternaetsiinf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ligainternaetsiinf.model.Partido;

import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {

   

}
