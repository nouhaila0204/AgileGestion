package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.service.core.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestion des tâches")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle tâche")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tâche créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PatchMapping("/{id}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        Task updated = taskService.startTask(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        Task updated = taskService.completeTask(id, notes);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reassign")
    public ResponseEntity<Task> reassignTask(
            @PathVariable Long id,
            @RequestParam Long newUserId) {
        Task updated = taskService.reassignTask(id, newUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/move-to-sprint")
    public ResponseEntity<Task> moveTaskToSprint(
            @PathVariable Long id,
            @RequestParam Long sprintId) {
        Task updated = taskService.moveTaskToSprint(id, sprintId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/overdue")
    public ResponseEntity<Map<String, Boolean>> isTaskOverdue(@PathVariable Long id) {
        boolean isOverdue = taskService.isTaskOverdue(id);
        return ResponseEntity.ok(Map.of("overdue", isOverdue));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une tâche par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche trouvée"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    public ResponseEntity<Task> getTaskById(
            @PathVariable @Parameter(description = "ID de la tâche") Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les tâches")
    @ApiResponse(responseCode = "200", description = "Liste des tâches récupérée")
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une tâche")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche mise à jour"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task) {
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une tâche")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tâche supprimée"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les tâches par statut")
    public ResponseEntity<List<Task>> getTasksByStatus(
            @PathVariable Status status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user-story/{userStoryId}")
    @Operation(summary = "Récupérer les tâches d'une user story")
    public ResponseEntity<List<Task>> getTasksByUserStory(
            @PathVariable Long userStoryId) {
        List<Task> tasks = taskService.getTasksByUserStory(userStoryId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/developer/{userId}")
    @Operation(summary = "Récupérer les tâches assignées à un développeur")
    public ResponseEntity<List<Task>> getTasksByDeveloper(
            @PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByDeveloper(userId);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{taskId}/assign/{userId}")
    @Operation(summary = "Assigner une tâche à un développeur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche assignée"),
            @ApiResponse(responseCode = "404", description = "Tâche ou utilisateur non trouvé"),
            @ApiResponse(responseCode = "400", description = "Utilisateur n'est pas un développeur")
    })
    public ResponseEntity<Task> assignTaskToDeveloper(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        Task task = taskService.assignTaskToDeveloper(taskId, userId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une tâche")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam Status status) {
        Task updated = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/elapsed-time")
    @Operation(summary = "Calculer le temps écoulé d'une tâche")
    public ResponseEntity<Map<String, Long>> getElapsedTime(@PathVariable Long id) {
        long elapsedTime = taskService.getTempsEcoule(id);
        return ResponseEntity.ok(Map.of("elapsedTimeDays", elapsedTime));
    }

    @GetMapping("/user-story/{userStoryId}/completion-rate")
    @Operation(summary = "Calculer le taux de complétion des tâches d'une user story")
    public ResponseEntity<Map<String, Double>> getTaskCompletionRate(
            @PathVariable Long userStoryId) {
        double completionRate = taskService.getTaskCompletionRate(userStoryId);
        return ResponseEntity.ok(Map.of("completionRate", completionRate));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur interne du serveur"));
    }
}