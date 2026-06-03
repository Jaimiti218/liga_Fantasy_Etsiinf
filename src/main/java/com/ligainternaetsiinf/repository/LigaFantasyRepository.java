package com.ligainternaetsiinf.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.ligainternaetsiinf.model.LigaFantasy;


import java.util.Optional;

public interface LigaFantasyRepository extends JpaRepository<LigaFantasy, Integer> {

    Optional<LigaFantasy> findByCode(String code);


}
