package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.service.core.prioritization.BacklogStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class BacklogStatisticsController {

    private final BacklogStatisticsService statisticsService;

    @Autowired
    public BacklogStatisticsController(BacklogStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<?> getBacklogStatistics(@PathVariable Long backlogId) {
        try {
            Map<String, Object> stats = statisticsService.getBacklogStatistics(backlogId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération des statistiques", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/prioritization-report")
    public ResponseEntity<?> getPrioritizationReport(@PathVariable Long backlogId) {
        try {
            List<Map<String, Object>> report = statisticsService.getPrioritizationReport(backlogId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du rapport de priorisation", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/total-points")
    public ResponseEntity<?> getTotalStoryPoints(@PathVariable Long backlogId) {
        try {
            Integer totalPoints = statisticsService.getTotalStoryPoints(backlogId);
            return ResponseEntity.ok(totalPoints);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du calcul des points", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/count-by-status")
    public ResponseEntity<?> countByStatus(
            @PathVariable Long backlogId,
            @RequestParam Status status) {
        try {
            Long count = statisticsService.countByStatus(backlogId, status);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Statut invalide", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du comptage par statut", "message", e.getMessage()));
        }
    }
}