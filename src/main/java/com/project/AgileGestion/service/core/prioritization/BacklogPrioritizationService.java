package com.project.AgileGestion.service.core.prioritization;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.PrioritizationMethod;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class BacklogPrioritizationService {

    private final StoryPrioritizationService storyPrioritizationService;

    public BacklogPrioritizationService(
            StoryPrioritizationService storyPrioritizationService) {
        this.storyPrioritizationService = storyPrioritizationService;
    }

    public List<UserStory> prioritize(
            List<UserStory> stories,
            PrioritizationMethod method) {

        return switch (method) {
            case MOSCOW -> sortByMoSCoW(stories);
            case WSJF -> sortByWSJF(stories);
            case VALUE_EFFORT -> sortByValueEffort(stories);
        };
    }

    /* ===== Méthodes privées d'organisation ===== */

    private List<UserStory> sortByMoSCoW(List<UserStory> stories) {
        Map<PriorityLevel, Integer> order = Map.of(
                PriorityLevel.MUST_HAVE, 1,
                PriorityLevel.SHOULD_HAVE, 2,
                PriorityLevel.COULD_HAVE, 3,
                PriorityLevel.WONT_HAVE, 4
        );

        return stories.stream()
                .sorted(Comparator.comparing(
                        s -> order.get(s.getPriorityLevel())))
                .toList();
    }

    private List<UserStory> sortByWSJF(List<UserStory> stories) {
        return stories.stream()
                .sorted(Comparator.comparing(
                        s -> storyPrioritizationService.calculateWSJF(s.getId()),
                        Comparator.reverseOrder()))
                .toList();
    }

    private List<UserStory> sortByValueEffort(List<UserStory> stories) {
        return stories.stream()
                .sorted(Comparator.comparing(
                        s -> storyPrioritizationService.calculateValueEffortRatio(s.getId()),
                        Comparator.reverseOrder()))
                .toList();
    }
}
