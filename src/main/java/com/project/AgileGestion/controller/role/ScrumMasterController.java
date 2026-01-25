package com.project.AgileGestion.controller.role;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.service.role.ScrumMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scrum-master")
@RequiredArgsConstructor
@Tag(name = "Scrum Master", description = "Endpoints spécifiques au Scrum Master")
public class ScrumMasterController {

    private final ScrumMasterService scrumMasterService;

    // ========== GESTION DES SPRINTS ==========

    @PostMapping("/sprints")
    @Operation(summary = "Créer un nouveau sprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sprint créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<SprintBacklog> createNewSprint(
            @RequestParam String nom,
            @RequestParam int dureeJours) {
        SprintBacklog sprint = scrumMasterService.createNewSprint(nom, dureeJours);
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
    }

    @PutMapping("/sprints/{sprintId}/start")
    @Operation(summary = "Démarrer un sprint")
    public ResponseEntity<SprintBacklog> startSprint(@PathVariable Long sprintId) {
        SprintBacklog sprint = scrumMasterService.startSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }

    @PutMapping("/sprints/{sprintId}/complete")
    @Operation(summary = "Terminer un sprint")
    public ResponseEntity<SprintBacklog> completeSprint(@PathVariable Long sprintId) {
        SprintBacklog sprint = scrumMasterService.completeSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }

    @PutMapping("/sprints/{sprintId}")
    @Operation(summary = "Mettre à jour un sprint")
    public ResponseEntity<SprintBacklog> updateSprint(
            @PathVariable Long sprintId,
            @RequestBody SprintBacklog sprintUpdate) {
        SprintBacklog updated = scrumMasterService.updateSprint(sprintId, sprintUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/sprints/{sprintId}")
    @Operation(summary = "Supprimer un sprint")
    @ApiResponse(responseCode = "204", description = "Sprint supprimé")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long sprintId) {
        scrumMasterService.deleteSprint(sprintId);
        return ResponseEntity.noContent().build();
    }

    // ========== GESTION DES USER STORIES DANS LES SPRINTS ==========

    @PostMapping("/sprints/{sprintId}/stories/{storyId}")
    @Operation(summary = "Ajouter une User Story à un sprint")
    public ResponseEntity<Void> addUserStoryToSprint(
            @PathVariable Long sprintId,
            @PathVariable Long storyId) {
        scrumMasterService.addUserStoryToSprint(sprintId, storyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sprints/{sprintId}/stories/{storyId}")
    @Operation(summary = "Retirer une User Story d'un sprint")
    public ResponseEntity<Void> removeUserStoryFromSprint(
            @PathVariable Long sprintId,
            @PathVariable Long storyId) {
        scrumMasterService.removeUserStoryFromSprint(sprintId, storyId);
        return ResponseEntity.ok().build();
    }

    // ========== SUIVI DE L'AVANCEMENT ==========

    @GetMapping("/sprints/{sprintId}/progress")
    @Operation(summary = "Obtenir la progression d'un sprint")
    public ResponseEntity<ScrumMasterService.SprintProgress> getSprintProgress(@PathVariable Long sprintId) {
        ScrumMasterService.SprintProgress progress = scrumMasterService.getSprintProgress(sprintId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/sprints/{sprintId}/standup-report")
    @Operation(summary = "Générer un rapport de daily standup")
    public ResponseEntity<ScrumMasterService.DailyStandupReport> generateDailyStandupReport(@PathVariable Long sprintId) {
        ScrumMasterService.DailyStandupReport report = scrumMasterService.generateDailyStandupReport(sprintId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sprints/{sprintId}/retrospective")
    @Operation(summary = "Générer un rapport de rétrospective")
    public ResponseEntity<ScrumMasterService.RetrospectiveReport> generateRetrospectiveReport(@PathVariable Long sprintId) {
        ScrumMasterService.RetrospectiveReport report = scrumMasterService.generateRetrospectiveReport(sprintId);
        return ResponseEntity.ok(report);
    }

    // ========== UTILITAIRES ==========

    @GetMapping("/sprints/{sprintId}/stories")
    @Operation(summary = "Récupérer les User Stories d'un sprint")
    public ResponseEntity<List<UserStory>> getSprintBacklog(@PathVariable Long sprintId) {
        List<UserStory> stories = scrumMasterService.getSprintBacklog(sprintId);
        return ResponseEntity.ok(stories);
    }

    @PatchMapping("/sprints/{sprintId}/name")
    @Operation(summary = "Mettre à jour le nom d'un sprint")
    public ResponseEntity<Void> updateSprintName(
            @PathVariable Long sprintId,
            @RequestParam String newName) {
        scrumMasterService.updateSprintName(sprintId, newName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/sprints/{sprintId}/dates")
    @Operation(summary = "Mettre à jour les dates d'un sprint")
    public ResponseEntity<Void> updateSprintDates(
            @PathVariable Long sprintId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        scrumMasterService.updateSprintDates(sprintId, startDate, endDate);
        return ResponseEntity.ok().build();
    }

    // ========== GESTION DES ERREURS ==========

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur interne du serveur"));
    }
}