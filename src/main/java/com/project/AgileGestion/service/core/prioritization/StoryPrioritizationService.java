package com.project.AgileGestion.service.core.prioritization;

import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import com.project.AgileGestion.service.core.UserStoryService;
import org.springframework.stereotype.Service;

/**
 * Service dédié aux calculs de priorisation
 * Utilisé par ProductOwnerService
 */
@Service
public class StoryPrioritizationService {

    private final UserStoryService userStoryService;

    public StoryPrioritizationService(UserStoryService userStoryService) {
        this.userStoryService = userStoryService;
    }

    // ========== CALCULS DE PRIORISATION ==========

    public Integer calculatePriorityScore(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);
        int businessValue = getBusinessValueScore(story.getPriorityLevel());
        int urgency = calculateUrgencyScore(story.getPriorite());
        int complexity = story.getStoryPoints() != null ? story.getStoryPoints() : 0;
        return (businessValue * 3) + urgency - complexity;
    }

    public Double calculateWSJF(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);
        int businessValue = getBusinessValueScore(story.getPriorityLevel());
        int timeCriticality = calculateUrgencyScore(story.getPriorite());
        int riskReduction = estimateRiskReduction(story);
        int jobSize = story.getStoryPoints() != null ? story.getStoryPoints() : 0;
        if (jobSize == 0) jobSize = 1;
        return (double) (businessValue + timeCriticality + riskReduction) / jobSize;
    }

    public Double calculateValueEffortRatio(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);
        int value = getBusinessValueScore(story.getPriorityLevel());
        int effort = story.getStoryPoints() != null ? story.getStoryPoints() : 0;
        if (effort == 0) effort = 1;
        return (double) value / effort;
    }

    // ========== MÉTHODES PRIVÉES ==========

    private int getBusinessValueScore(PriorityLevel priorityLevel) {
        switch (priorityLevel) {
            case MUST_HAVE: return 4;
            case SHOULD_HAVE: return 3;
            case COULD_HAVE: return 2;
            case WONT_HAVE: return 1;
            default: return 2;
        }
    }

    private int calculateUrgencyScore(Integer priority) {
        if (priority == null) return 5;
        int adjustedPriority = Math.max(1, Math.min(priority, 10));
        return 11 - adjustedPriority;
    }

    private int estimateRiskReduction(UserStory story) {
        int baseRisk = 3;
        if (story.getStoryPoints() != null && story.getStoryPoints() >= 13) {
            baseRisk += 2;
        }
        if (story.getDescription() == null || story.getDescription().length() < 20) {
            baseRisk += 1;
        }
        return baseRisk;
    }
}