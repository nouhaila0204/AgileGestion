package com.project.AgileGestion.repository;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SprintBacklogRepository extends JpaRepository<SprintBacklog, Long> {

    // CORRECTION : Utiliser findByStatus directement
    List<SprintBacklog> findByStatus(Status status);

    // Méthode pour les sprints actifs (alternative)
    default List<SprintBacklog> findActiveSprints() {
        return findByStatus(Status.IN_PROGRESS);
    }

    // OU avec annotation Query (version paramétrée)
    @Query("SELECT sb FROM SprintBacklog sb WHERE sb.status = :status")
    List<SprintBacklog> findByStatusCustom(@Param("status") Status status);

    List<SprintBacklog> findByNomContaining(String nom);

    // Recherche par période
    List<SprintBacklog> findByDateDebutBetween(LocalDate start, LocalDate end);

    List<SprintBacklog> findByDateFinBetween(LocalDate start, LocalDate end);

    // Sprints en cours à une date donnée
    @Query("SELECT sb FROM SprintBacklog sb WHERE sb.dateDebut <= :date AND sb.dateFin >= :date")
    List<SprintBacklog> findSprintsActiveOnDate(@Param("date") LocalDate date);

    // Sprints par durée
    @Query("SELECT sb FROM SprintBacklog sb WHERE sb.dureeJours = :duration")
    List<SprintBacklog> findByDuration(@Param("duration") Integer duration);

    // Compter les sprints par statut
    long countByStatus(Status status);

    // Trouver le sprint le plus récent
    @Query("SELECT sb FROM SprintBacklog sb ORDER BY sb.dateDebut DESC LIMIT 1")
    SprintBacklog findLatestSprint();

    // Sprints avec un certain nombre de User Stories
    @Query("SELECT sb FROM SprintBacklog sb WHERE SIZE(sb.userStories) >= :minStories")
    List<SprintBacklog> findSprintsWithMinStories(@Param("minStories") int minStories);
}