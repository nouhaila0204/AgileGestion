package com.project.AgileGestion.service.core.prioritization;

import com.project.AgileGestion.entity.Epic;
import com.project.AgileGestion.entity.ProductBacklog;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.service.core.EpicService;
import com.project.AgileGestion.service.core.ProductBacklogService;
import com.project.AgileGestion.service.core.StoryValidator;
import com.project.AgileGestion.service.core.UserStoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service CORE pour les dashboards et rapports du backlog
 * Responsabilité : Agréger les données de plusieurs services CORE
 */
@Service
public class BacklogDashboardService {

    private final ProductBacklogService productBacklogService;
    private final UserStoryService userStoryService;
    private final EpicService epicService;
    private final BacklogStatisticsService statisticsService;
    private final StoryValidator storyValidator;

    public BacklogDashboardService(
            ProductBacklogService productBacklogService,
            UserStoryService userStoryService,
            EpicService epicService,
            BacklogStatisticsService statisticsService,
            StoryValidator storyValidator) {
        this.productBacklogService = productBacklogService;
        this.userStoryService = userStoryService;
        this.epicService = epicService;
        this.statisticsService = statisticsService;
        this.storyValidator = storyValidator;
    }

    // ========== DASHBOARDS ET RAPPORTS ==========

    /**
     * Tableau de bord complet du backlog (4.1)
     */
    public Map<String, Object> getBacklogDashboard(Long backlogId) {
        ProductBacklog backlog = productBacklogService.getProductBacklogById(backlogId);

        Map<String, Object> dashboard = new HashMap<>();

        // 1. Informations de base
        dashboard.put("backlogInfo", Map.of(
                "id", backlog.getId(),
                "nom", backlog.getNom(),
                "description", backlog.getDescription() != null ? backlog.getDescription() : "",
                "projectId", backlog.getProjectId(),
                "createdAt", backlog.getCreatedAt()
        ));

        // 2. Statistiques
        Map<String, Object> stats = statisticsService.getBacklogStatistics(backlogId);
        dashboard.put("statistiques", stats);

        // 3. Métriques
        Map<String, Object> metrics = calculateMetrics(backlogId);
        dashboard.put("metriques", metrics);

        // 4. Listes importantes
        Map<String, Object> lists = new HashMap<>();
        lists.put("epics", epicService.getEpicsWithStories(backlogId));
        lists.put("readyForSprint", getStoriesReadyForSprint(backlogId));
        lists.put("unorganizedStories", getUnorganizedStories(backlogId));
        dashboard.put("listes", lists);

        dashboard.put("generatedAt", new Date());

        return dashboard;
    }

    /**
     * Rapport pour Product Owner (8 - Reporting)
     */
    public Map<String, Object> generateProductOwnerReport(Long backlogId) {
        ProductBacklog backlog = productBacklogService.getProductBacklogById(backlogId);
        Map<String, Object> stats = statisticsService.getBacklogStatistics(backlogId);
        Map<String, Object> metrics = calculateMetrics(backlogId);

        Map<String, Object> report = new HashMap<>();
        report.put("reportId", UUID.randomUUID().toString());
        report.put("generatedAt", java.time.LocalDateTime.now());

        report.put("backlogInfo", Map.of(
                "id", backlog.getId(),
                "nom", backlog.getNom(),
                "description", backlog.getDescription() != null ? backlog.getDescription() : "",
                "projectId", backlog.getProjectId()
        ));

        report.put("statistiques", stats);
        report.put("metriques", metrics);

        Map<String, Object> readiness = new HashMap<>();
        readiness.put("totalStories", stats.get("totalStories"));
        readiness.put("readyForSprint", getStoriesReadyForSprint(backlogId).size());
        report.put("etatPreparation", readiness);

        return report;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Calculer les métriques du backlog
     */
    private Map<String, Object> calculateMetrics(Long backlogId) {
        List<Epic> epics = epicService.getEpicsByBacklogId(backlogId);
        List<UserStory> stories = userStoryService.getByProductBacklog(backlogId);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalEpics", epics.size());
        metrics.put("totalStories", stories.size());
        metrics.put("totalStoryPoints", statisticsService.getTotalStoryPoints(backlogId));

        int epicsWithoutStories = 0;
        for (Epic epic : epics) {
            List<UserStory> epicStories = epicService.getUserStoriesForEpic(epic.getId());
            if (epicStories.isEmpty()) {
                epicsWithoutStories++;
            }
        }

        metrics.put("epicsWithoutStories", epicsWithoutStories);
        metrics.put("epicsWithStories", epics.size() - epicsWithoutStories);

        return metrics;
    }

    /**
     * Obtenir les stories prêtes pour sprint
     */
    private List<UserStory> getStoriesReadyForSprint(Long backlogId) {
        List<UserStory> stories = userStoryService.getByProductBacklog(backlogId);
        return stories.stream()
                .filter(storyValidator::isReadyForSprint)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les stories non organisées (sans Epic)
     */
    private List<UserStory> getUnorganizedStories(Long backlogId) {
        List<UserStory> stories = userStoryService.getByProductBacklog(backlogId);
        return stories.stream()
                .filter(story -> story.getEpicId() == null)
                .collect(Collectors.toList());
    }
}