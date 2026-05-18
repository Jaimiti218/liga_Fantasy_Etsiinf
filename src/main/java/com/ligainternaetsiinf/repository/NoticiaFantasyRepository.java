package com.ligainternaetsiinf.repository;

import com.ligainternaetsiinf.model.NoticiaFantasy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NoticiaFantasyRepository extends JpaRepository<NoticiaFantasy, Integer> {

    // Noticias públicas de una liga
    List<NoticiaFantasy> findByLigaFantasyIdAndUserIdDestinatarioIsNullOrderByFechaDescHoraDesc(
        Integer ligaId);

    // Noticias privadas de un usuario en una liga
    List<NoticiaFantasy> findByLigaFantasyIdAndUserIdDestinatarioOrderByFechaDescHoraDesc(
        Integer ligaId, Integer userId);

    // Todas las noticias visibles para un usuario (públicas + sus privadas)
    @Query("SELECT n FROM NoticiaFantasy n WHERE n.ligaFantasy.id = :ligaId " +
           "AND (n.userIdDestinatario IS NULL OR n.userIdDestinatario = :userId) " +
           "ORDER BY n.fecha DESC, n.hora DESC")
    List<NoticiaFantasy> findNoticiasVisibles(
        @Param("ligaId") Integer ligaId,
        @Param("userId") Integer userId);
}