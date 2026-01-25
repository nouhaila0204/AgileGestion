package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import org.springframework.stereotype.Component;

/**
 * Validateur pour les règles métier des User Stories
 */
@Component
public class StoryValidator {

    public boolean isReadyForSprint(UserStory story) {
        return hasAcceptanceCriteria(story)
                && hasStoryPoints(story)
                && isNotWontHave(story);
    }

    private boolean hasAcceptanceCriteria(UserStory story) {
        return story.getCriteresAcceptation() != null
                && !story.getCriteresAcceptation().isEmpty();
    }

    private boolean hasStoryPoints(UserStory story) {
        return story.getStoryPoints() != null && story.getStoryPoints() > 0;
    }

    private boolean isNotWontHave(UserStory story) {
        return story.getPriorityLevel() != PriorityLevel.WONT_HAVE;
    }
}