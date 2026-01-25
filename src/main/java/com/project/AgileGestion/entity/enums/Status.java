package com.project.AgileGestion.entity.enums;

import lombok.Getter;

@Getter
public enum Status {
    TODO("À faire"),
    IN_PROGRESS("En cours"),
    IN_TEST("en test"),
    BLOCKED("bloqué"),
    DONE("Terminée");

    private final String label;
    Status(String label) { this.label = label; }
}