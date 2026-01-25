package com.project.AgileGestion.repository;

import com.project.AgileGestion.entity.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {

    // Récupérer tous les Epics d'un ProductBacklog (Cahier 4.3)
    List<Epic> findByBacklogId(Long backlogId);

    // Compter le nombre d'Epics par backlog
    Long countByBacklogId(Long backlogId);

    // Rechercher par titre (filtrage)
    List<Epic> findByBacklogIdAndTitreContainingIgnoreCase(Long backlogId, String keyword);

    // Récupérer Epic avec ses UserStories (Cahier 4.3)
    @Query("SELECT e FROM Epic e LEFT JOIN FETCH e.userStories WHERE e.id = :id")
    Optional<Epic> findByIdWithUserStories(@Param("id") Long id);

    // Vérifier l'existence d'un Epic dans un backlog
    boolean existsByIdAndBacklogId(Long id, Long backlogId);

    // Trouver les Epics sans UserStories
    @Query("SELECT e FROM Epic e WHERE e.backlogId = :backlogId AND e.userStories IS EMPTY")
    List<Epic> findEpicsWithoutStories(@Param("backlogId") Long backlogId);

    // Trouver les Epics avec plus de X UserStories
    @Query("SELECT e FROM Epic e WHERE e.backlogId = :backlogId AND SIZE(e.userStories) > :minStories")
    List<Epic> findEpicsWithMoreThanXStories(@Param("backlogId") Long backlogId, @Param("minStories") int minStories);

    // Récupérer les derniers Epics créés
    List<Epic> findByBacklogIdOrderByCreatedAtDesc(Long backlogId);

    // Vérifier si un titre d'Epic existe déjà dans un backlog
    boolean existsByBacklogIdAndTitre(Long backlogId, String titre);
}