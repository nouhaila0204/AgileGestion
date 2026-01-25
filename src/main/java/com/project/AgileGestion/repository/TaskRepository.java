package com.project.AgileGestion.repository;

import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.enums.Status; // CORRECTION : Status au lieu de Statut
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(Status status);

    // Option 1: Par l'ID de l'utilisateur assigné
    List<Task> findByAssignedToUserId(Long userId);


    long countByUserStoryIdAndStatus(Long userStoryId, Status status);

    // Méthodes supplémentaires utiles
    List<Task> findBySprintBacklogId(Long sprintId);

    @Query("SELECT t FROM Task t WHERE t.userStoryId = :userStoryId")
    List<Task> findByUserStoryId(@Param("userStoryId") Long userStoryId);


    @Query("SELECT t FROM Task t WHERE t.estimationHeures > :hours")
    List<Task> findTasksWithEstimationGreaterThan(@Param("hours") Integer hours);

    @Query("SELECT t FROM Task t WHERE t.dateCompletion IS NULL AND t.dateCreation < :date")
    List<Task> findOverdueTasks(@Param("date") java.time.LocalDate date);

    // ⭐ CORRECTION : La requête était incorrecte (sprintBacklog.id, pas sprintBacklog)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprintBacklog.id = :sprintId AND t.status = :status")
    long countBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") Status status);

    // ⭐ AJOUT : Méthodes utiles supplémentaires
    List<Task> findByAssignedToUserIdIsNull();

    List<Task> findBySprintBacklogIsNull();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.userStoryId = :userStoryId")
    long countByUserStoryId(@Param("userStoryId") Long userStoryId);

}