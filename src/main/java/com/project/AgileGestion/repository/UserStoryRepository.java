package com.project.AgileGestion.repository;

import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import com.project.AgileGestion.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository  // ⭐ Dit à Spring : "cette classe gère l'accès aux données"
// ⭐ JpaRepository<UserStory, Long> = méthodes CRUD gratuites pour UserStory avec ID Long
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    // ⭐⭐ MÉTHODES PERSONNALISÉES (Spring génère le SQL automatiquement !)

    // Trouver toutes les UserStories d'un ProductBacklog
    // SQL généré : SELECT * FROM user_stories WHERE product_backlog_id = ?
    List<UserStory> findByBacklogId(Long backlogId);

    // Trouver toutes les UserStories d'un Epic
    List<UserStory> findByEpicId(Long epicId);

    // Trouver toutes les UserStories d'un Sprint
    List<UserStory> findBySprintBacklogId(Long sprintBacklogId);

    // Trouver par statut
    List<UserStory> findByStatut(Status statut);

    // Trouver et trier par priorité (1 = plus haute)
    List<UserStory> findByBacklogIdOrderByPrioriteAsc(Long backlogId);

    // ⭐ REQUÊTE PERSONNALISÉE (si la méthode nommée ne suffit pas)
    @Query("SELECT COALESCE(SUM(us.storyPoints), 0) FROM UserStory us WHERE us.backlogId = :backlogId")
    Integer sumStoryPointsByBacklogId(Long backlogId);
    // ⭐ COALESCE = si pas de résultat, retourne 0

    // Compter combien de stories ont un certain statut dans un backlog
    Long countByBacklogIdAndStatut(Long backlogId, Status statut);

    // Compter les UserStories par niveau de priorité
    Long countByBacklogIdAndPriorityLevel(Long backlogId, PriorityLevel priorityLevel);
}