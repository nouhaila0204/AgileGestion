package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.service.core.SprintBacklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Backlog", description = "Gestion des sprints")
public class SprintBacklogController {

    private final SprintBacklogService sprintBacklogService;

    @PostMapping
    @Operation(summary = "Créer un nouveau sprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sprint créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Conflit de données")
    })
    public ResponseEntity<SprintBacklog> createSprintBacklog(
            @Valid @RequestBody SprintBacklog sprintBacklog) {
        SprintBacklog created = sprintBacklogService.createSprintBacklog(sprintBacklog);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un sprint par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sprint trouvé"),
            @ApiResponse(responseCode = "404", description = "Sprint non trouvé")
    })
    public ResponseEntity<SprintBacklog> getSprintBacklogById(
            @PathVariable @Parameter(description = "ID du sprint") Long id) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(id);
        return ResponseEntity.ok(sprint);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les sprints")
    @ApiResponse(responseCode = "200", description = "Liste des sprints récupérée")
    public ResponseEntity<List<SprintBacklog>> getAllSprintBacklogs() {
        List<SprintBacklog> sprints = sprintBacklogService.getAllSprintBacklogs();
        return ResponseEntity.ok(sprints);
    }

    @GetMapping("/active")
    @Operation(summary = "Récupérer les sprints actifs")
    public ResponseEntity<List<SprintBacklog>> getActiveSprints() {
        List<SprintBacklog> activeSprints = sprintBacklogService.getActiveSprints();
        return ResponseEntity.ok(activeSprints);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un sprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sprint mis à jour"),
            @ApiResponse(responseCode = "404", description = "Sprint non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<SprintBacklog> updateSprintBacklog(
            @PathVariable Long id,
            @Valid @RequestBody SprintBacklog sprintBacklog) {
        SprintBacklog updated = sprintBacklogService.updateSprintBacklog(id, sprintBacklog);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'un sprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
            @ApiResponse(responseCode = "404", description = "Sprint non trouvé"),
            @ApiResponse(responseCode = "400", description = "Transition de statut invalide")
    })
    public ResponseEntity<SprintBacklog> updateSprintStatus(
            @PathVariable Long id,
            @RequestParam Status status) {
        SprintBacklog updated = sprintBacklogService.updateSprintStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un sprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sprint supprimé"),
            @ApiResponse(responseCode = "404", description = "Sprint non trouvé"),
            @ApiResponse(responseCode = "409", description = "Conflit lors de la suppression")
    })
    public ResponseEntity<Void> deleteSprintBacklog(@PathVariable Long id) {
        sprintBacklogService.deleteSprintBacklog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/velocity")
    @Operation(summary = "Calculer la vélocité d'un sprint")
    public ResponseEntity<Map<String, Double>> calculateVelocity(@PathVariable Long id) {
        double velocity = sprintBacklogService.calculateVelocite(id);
        double progress = sprintBacklogService.calculateProgress(id);

        return ResponseEntity.ok(Map.of(
                "velocity", velocity,
                "progress", progress
        ));
    }

    @GetMapping("/{id}/remaining-days")
    @Operation(summary = "Calculer les jours restants d'un sprint")
    public ResponseEntity<Map<String, Integer>> getRemainingDays(@PathVariable Long id) {
        int remainingDays = sprintBacklogService.calculateRemainingDays(id);
        return ResponseEntity.ok(Map.of("remainingDays", remainingDays));
    }

    @GetMapping("/{id}/user-stories")
    @Operation(summary = "Récupérer les user stories d'un sprint")
    public ResponseEntity<?> getUserStoriesInSprint(@PathVariable Long id) {
        try {
            var userStories = sprintBacklogService.getUserStoriesInSprint(id);
            return ResponseEntity.ok(userStories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des user stories"));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}