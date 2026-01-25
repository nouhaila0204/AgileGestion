package com.project.AgileGestion.service.role;

import com.project.AgileGestion.entity.Epic;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.PrioritizationMethod;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import com.project.AgileGestion.service.core.*;
import com.project.AgileGestion.service.core.prioritization.BacklogDashboardService;
import com.project.AgileGestion.service.core.prioritization.BacklogPrioritizationService;
import com.project.AgileGestion.service.core.prioritization.StoryPrioritizationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service ROLE pour le Product Owner
 * Responsabilité : Priorisation, gestion du backlog, définition des Epics
 */
@Service
public class ProductOwnerService {

    private final UserStoryService userStoryService;
    private final EpicService epicService;
    private final BacklogDashboardService dashboardService; // ✅ NOUVEAU
    private final StoryPrioritizationService prioritizationService;
    private final StoryValidator storyValidator;
    private final BacklogPrioritizationService backlogPrioritizationService;

    public ProductOwnerService(
            UserStoryService userStoryService,
            EpicService epicService,
            BacklogDashboardService dashboardService,
            StoryPrioritizationService prioritizationService,
            StoryValidator storyValidator,
            BacklogPrioritizationService backlogPrioritizationService) {
        this.userStoryService = userStoryService;
        this.epicService = epicService;
        this.dashboardService = dashboardService;
        this.prioritizationService = prioritizationService;
        this.storyValidator = storyValidator;
        this.backlogPrioritizationService = backlogPrioritizationService;
    }

    // ========== GESTION DES PRIORITÉS (4.2) ==========

    public UserStory updateStoryPriority(Long storyId, PriorityLevel priorityLevel) {
        return userStoryService.updatePriorityLevel(storyId, priorityLevel);
    }

    public UserStory updatePriorityNumber(Long storyId, Integer priorityNumber) {
        return userStoryService.updatePriority(storyId, priorityNumber);
    }

    // ========== PRIORISATION DU BACKLOG (4.1, 4.2) ==========

    public List<UserStory> getPrioritizedBacklog(
            Long backlogId,
            PrioritizationMethod method) {

        List<UserStory> stories =
                userStoryService.getByProductBacklog(backlogId);

        return backlogPrioritizationService.prioritize(stories, method);
    }


    public List<UserStory> getSortedByPriority(Long backlogId) {
        return userStoryService.getSortedByPriority(backlogId);
    }

    // ========== DASHBOARDS ET RAPPORTS ==========

    /**
     * Obtenir le dashboard complet du backlog (4.1)
     */
    public Map<String, Object> getBacklogDashboard(Long backlogId) {
        return dashboardService.getBacklogDashboard(backlogId);
    }
    /**
     * Générer un rapport pour le Product Owner (8)
     */
    public Map<String, Object> generateReport(Long backlogId) {
        return dashboardService.generateProductOwnerReport(backlogId);
    }

    // ========== GESTION DES EPICS (4.3) ==========

    public Epic createEpic(Epic epic) {
        if (epic.getDescription() == null || epic.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description de l'Epic est obligatoire");
        }
        return epicService.createEpic(epic);
    }

    public void organizeStoriesIntoEpic(Long epicId, List<Long> storyIds) {
        epicService.associateMultipleUserStoriesToEpic(epicId, storyIds);
    }

    public List<Epic> getEpicsForBacklog(Long backlogId) {
        return epicService.getEpicsByBacklogId(backlogId);
    }

    public List<UserStory> getStoriesForEpic(Long epicId) {
        return epicService.getUserStoriesForEpic(epicId);
    }
    /**
     * Organisation du backlog par Epics (4.1 + 4.3)
     */
    public Map<String, Object> getBacklogOrganization(Long backlogId) {
        List<Epic> epics = epicService.getEpicsByBacklogId(backlogId);
        List<UserStory> allStories = userStoryService.getByProductBacklog(backlogId);

        List<UserStory> organizedStories = new ArrayList<>();
        List<UserStory> unorganizedStories = allStories.stream()
                .filter(story -> story.getEpicId() == null)
                .collect(Collectors.toList());

        Map<String, Object> organization = new HashMap<>();
        List<Map<String, Object>> epicDetails = new ArrayList<>();

        for (Epic epic : epics) {
            Map<String, Object> epicData = new HashMap<>();
            epicData.put("epic", epic);

            List<UserStory> epicStories = epicService.getUserStoriesForEpic(epic.getId());
            epicData.put("stories", epicStories);
            epicData.put("storyCount", epicStories.size());

            organizedStories.addAll(epicStories);
            epicDetails.add(epicData);
        }

        organization.put("epics", epicDetails);
        organization.put("totalEpics", epics.size());
        organization.put("organizedStories", organizedStories.size());
        organization.put("unorganizedStories", unorganizedStories.size());
        organization.put("unorganizedStoriesList", unorganizedStories);
        organization.put("organizationRate", allStories.isEmpty() ? "0%" :
                String.format("%.1f%%", (double) organizedStories.size() / allStories.size() * 100));

        return organization;
    }

    // ========== RECOMMANDATIONS (4.2) ==========

    public List<String> getPrioritizationRecommendations(Long backlogId) {
        List<UserStory> stories = userStoryService.getByProductBacklog(backlogId);
        List<String> recommendations = new ArrayList<>();

        long mustHaveCount = stories.stream()
                .filter(story -> story.getPriorityLevel() == PriorityLevel.MUST_HAVE)
                .count();

        if (mustHaveCount == 0) {
            recommendations.add("⚠️ Aucune UserStory MUST_HAVE.");
        } else if (mustHaveCount > 10) {
            recommendations.add("⚠️ Trop de MUST_HAVE (" + mustHaveCount + ").");
        }

        long wontHaveCount = stories.stream()
                .filter(story -> story.getPriorityLevel() == PriorityLevel.WONT_HAVE)
                .count();

        if (wontHaveCount > 5) {
            recommendations.add("📝 " + wontHaveCount + " stories WONT_HAVE.");
        }

        long unestimated = stories.stream()
                .filter(story -> story.getStoryPoints() == null || story.getStoryPoints() == 0)
                .count();

        if (unestimated > 0) {
            recommendations.add("🔢 " + unestimated + " stories non estimées.");
        }

        return recommendations;
    }
}