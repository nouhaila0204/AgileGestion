package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.service.core.prioritization.BacklogDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class BacklogDashboardController {

    private final BacklogDashboardService dashboardService;

    @Autowired
    public BacklogDashboardController(BacklogDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<?> getBacklogDashboard(@PathVariable Long backlogId) {
        try {
            Map<String, Object> dashboard = dashboardService.getBacklogDashboard(backlogId);
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du dashboard", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/product-owner-report")
    public ResponseEntity<?> generateProductOwnerReport(@PathVariable Long backlogId) {
        try {
            Map<String, Object> report = dashboardService.generateProductOwnerReport(backlogId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du rapport", "message", e.getMessage()));
        }
    }
}