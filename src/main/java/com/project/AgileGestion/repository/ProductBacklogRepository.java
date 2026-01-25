package com.project.AgileGestion.repository;

import com.project.AgileGestion.entity.ProductBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductBacklogRepository extends JpaRepository<ProductBacklog, Long> {

    // Trouver par projet (cahier des charges 4.1)
    List<ProductBacklog> findByProjectId(Long projectId);

    // Compter les backlogs par projet
    Long countByProjectId(Long projectId);

    // Statistiques avancées (pour reporting)
    @Query("SELECT COUNT(us) FROM UserStory us WHERE us.backlogId = :backlogId")
    Long countUserStoriesByBacklogId(Long backlogId);

    @Query("SELECT COUNT(e) FROM Epic e WHERE e.backlogId = :backlogId")
    Long countEpicsByBacklogId(Long backlogId);
}
