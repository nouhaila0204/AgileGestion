package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.UserStory;
import org.springframework.stereotype.Component;

/**
 * Formateur pour les User Stories
 */
@Component
public class StoryFormatter {

    public String formatAsStandard(UserStory story) {
        String titre = story.getTitre() != null ? story.getTitre() : "";
        String description = story.getDescription() != null ? story.getDescription() : "";

        String formatted = "En tant que [rôle], je veux " + titre;

        if (!description.isEmpty()) {
            if (description.length() > 100) {
                description = description.substring(0, 100) + "...";
            }
            formatted += ", afin de " + description;
        } else {
            formatted += ", afin de [réaliser un bénéfice métier]";
        }

        return formatted;
    }
}