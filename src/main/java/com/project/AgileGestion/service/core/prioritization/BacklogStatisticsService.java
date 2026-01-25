package com.project.AgileGestion.service.core.prioritization;

import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.service.core.StoryValidator;
import com.project.AgileGestion.service.core.UserStoryService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service dédié aux statistiques et rapports
 * Utilisé par ProductOwnerService et ScruMasterService
 */
@Service
public class BacklogStatisticsService {

    private final UserStoryService userStoryService;
    private final StoryPrioritizationService prioritizationService;
    private final StoryValidator storyValidator;

    public BacklogStatisticsService(
            UserStoryService userStoryService,
            StoryPrioritizationService prioritizationService,
            StoryValidator storyValidator) {
        this.userStoryService = userStoryService;
        this.prioritizationService = prioritizationService;
        this.storyValidator = storyValidator;
    }

    // ========== STATISTIQUES ==========

    public Map<String, Object> getBacklogStatistics(Long backlogId) {
        List<UserStory> stories = userStoryService.getByProductBacklog(backlogId);

        if (stories.isEmpty()) {
            return createEmptyStatistics();
        }

        Map<String, Long> statusCount = new HashMap<>();
        Map<String, Long> priorityCount = new HashMap<>();
        int totalPoints = 0;
        int doneCount = 0;

        for (UserStory story : stories) {
            updateStatusCount(statusCount, story);
            updatePriorityCount(priorityCount, story);
            totalPoints += story.getStoryPoints() != null ? story.getStoryPoints() : 0;
            if (story.getStatut() == Status.DONE) {
                doneCount++;
            }
        }

        return buildStatisticsMap(stories.size(), statusCount, priorityCount, totalPoints, doneCount);
    }

    public List<Map<String, Object>> getPrioritizationReport(Long backlogId) {
        List<UserStory> stories = userStoryService.getByProductBacklog(backlogId);
        List<Map<String, Object>> report = new ArrayList<>();

        for (UserStory story : stories) {
            Map<String, Object> storyData = createStoryReportData(story);
            report.add(storyData);
        }
        sortByRecommendedOrder(report);
        return report;
    }

    public Integer getTotalStoryPoints(Long backlogId) {
        return userStoryService.getByProductBacklog(backlogId).stream()
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();
    }

    public Long countByStatus(Long backlogId, Status status) {
        return userStoryService.getByProductBacklog(backlogId).stream()
                .filter(s -> s.getStatut() == status)
                .count();
    }

    // ========== MÉTHODES PRIVÉES ==========

    private Map<String, Object> createStoryReportData(UserStory story) {
        Double wsjf = prioritizationService.calculateWSJF(story.getId());
        Double valueEffort = prioritizationService.calculateValueEffortRatio(story.getId());
        Integer priorityScore = prioritizationService.calculatePriorityScore(story.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("id", story.getId());
        data.put("titre", story.getTitre());
        data.put("priorityLevel", story.getPriorityLevel().name());
        data.put("priority", story.getPriorite());
        data.put("priorityScore", priorityScore);
        data.put("wsjf", String.format("%.2f", wsjf));
        data.put("valueEffortRatio", String.format("%.2f", valueEffort));
        data.put("storyPoints", story.getStoryPoints());
        data.put("status", story.getStatut().name());
        data.put("recommendedOrder", (wsjf * 100) + priorityScore);
        data.put("isReadyForSprint", storyValidator.isReadyForSprint(story));

        return data;
    }

    private void sortByRecommendedOrder(List<Map<String, Object>> report) {
        report.sort((a, b) -> {
            Double scoreA = (Double) a.get("recommendedOrder");
            Double scoreB = (Double) b.get("recommendedOrder");
            return scoreB.compareTo(scoreA);
        });
    }

    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStories", 0L);
        stats.put("totalStoryPoints", 0L);
        stats.put("completionRate", "0.0%");
        stats.put("statusDistribution", new HashMap<>());
        stats.put("priorityDistribution", new HashMap<>());
        stats.put("mustHaveCount", 0L);
        stats.put("shouldHaveCount", 0L);
        stats.put("couldHaveCount", 0L);
        stats.put("wontHaveCount", 0L);
        return stats;
    }

    private void updateStatusCount(Map<String, Long> statusCount, UserStory story) {
        String status = story.getStatut().name();
        statusCount.merge(status, 1L, Long::sum);
    }

    private void updatePriorityCount(Map<String, Long> priorityCount, UserStory story) {
        String priority = story.getPriorityLevel().name();
        priorityCount.merge(priority, 1L, Long::sum);
    }

    private Map<String, Object> buildStatisticsMap(
            int totalStories,
            Map<String, Long> statusCount,
            Map<String, Long> priorityCount,
            int totalPoints,
            int doneCount) {

        double completionRate = (double) doneCount / totalStories * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("totalStories", (long) totalStories);
        result.put("statusDistribution", statusCount);
        result.put("priorityDistribution", priorityCount);
        result.put("totalStoryPoints", (long) totalPoints);
        result.put("completionRate", String.format("%.1f%%", completionRate));
        result.put("mustHaveCount", priorityCount.getOrDefault("MUST_HAVE", 0L));
        result.put("shouldHaveCount", priorityCount.getOrDefault("SHOULD_HAVE", 0L));
        result.put("couldHaveCount", priorityCount.getOrDefault("COULD_HAVE", 0L));
        result.put("wontHaveCount", priorityCount.getOrDefault("WONT_HAVE", 0L));

        return result;
    }
}