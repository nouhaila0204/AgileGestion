package com.project.AgileGestion.entity.enums;

import lombok.Getter;

@Getter
public enum PriorityLevel {
    MUST_HAVE("Doit avoir", 1),
    SHOULD_HAVE("Devrait avoir", 2),
    COULD_HAVE("Pourrait avoir", 3),
    WONT_HAVE("N'aura pas", 4);

    private final String label;
    private final int weight;

    PriorityLevel(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

}
