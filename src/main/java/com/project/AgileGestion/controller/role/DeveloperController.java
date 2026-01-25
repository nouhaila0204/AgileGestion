package com.project.AgileGestion.controller.role;

import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.service.core.TaskService;
import com.project.AgileGestion.service.role.DeveloperService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour les fonctionnalités du Développeur
 */
@RestController
@RequestMapping("/api/developer")
public class DeveloperController {

    private final DeveloperService developerService;
    private final TaskService taskService;

    @Autowired
    public DeveloperController(DeveloperService developerService, TaskService taskService) {
        this.developerService = developerService;
        this.taskService = taskService;
    }

    // ========== GESTION DU CYCLE DE VIE DES STORIES ==========

    @PutMapping("/stories/{storyId}/start")
    public ResponseEntity<?> startWorkingOnStory(@PathVariable Long storyId) {
        try {
            UserStory story = developerService.startWorkingOnStory(storyId);
            return ResponseEntity.ok(story);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Action non autorisée", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du démarrage du travail", "message", e.getMessage()));
        }
    }

    @PutMapping("/stories/{storyId}/complete")
    public ResponseEntity<?> completeStory(@PathVariable Long storyId) {
        try {
            UserStory story = developerService.completeStory(storyId);
            return ResponseEntity.ok(story);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Action non autorisée", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la complétion de la user story", "message", e.getMessage()));
        }
    }

    @PutMapping("/stories/{storyId}/block")
    public ResponseEntity<?> markStoryAsBlocked(@PathVariable Long storyId) {
        try {
            UserStory story = developerService.markStoryAsBlocked(storyId);
            return ResponseEntity.ok(story);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Action non autorisée", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du blocage de la user story", "message", e.getMessage()));
        }
    }

    // ========== VISUALISATION DES TÂCHES ==========


    @GetMapping("/backlogs/{backlogId}/my-stories")
    public ResponseEntity<?> getMyStoriesInBacklog(@PathVariable Long backlogId) {
        try {
            List<UserStory> stories = developerService.getMyStoriesInBacklog(backlogId);
            return ResponseEntity.ok(stories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de vos user stories", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlogs/{backlogId}/my-completed")
    public ResponseEntity<?> getMyCompletedStories(@PathVariable Long backlogId) {
        try {
            List<UserStory> stories = developerService.getMyCompletedStories(backlogId);
            return ResponseEntity.ok(stories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de vos user stories complétées", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlogs/{backlogId}/work-summary")
    public ResponseEntity<?> getWorkSummary(@PathVariable Long backlogId) {
        try {
            Map<String, Object> summary = developerService.getWorkSummary(backlogId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du résumé de travail", "message", e.getMessage()));
        }
    }

    // ========== OUTILS DE DÉVELOPPEMENT ==========

    @GetMapping("/stories/{storyId}/details")
    public ResponseEntity<?> getStoryDetailsForDevelopment(@PathVariable Long storyId) {
        try {
            Map<String, Object> details = developerService.getStoryDetailsForDevelopment(storyId);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des détails", "message", e.getMessage()));
        }
    }

    @PostMapping("/stories/{storyId}/tasks")
    @Operation(summary = "Créer une Task pour une User Story")
    public ResponseEntity<Task> createTaskForStory(
            @PathVariable Long storyId,
            @RequestBody Task task) {
        Task created = developerService.createTaskForStory(storyId, task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/stories/{storyId}/tasks")
    @Operation(summary = "Récupérer les Tasks d'une User Story")
    public ResponseEntity<List<Task>> getTasksByUserStory(@PathVariable Long storyId) {
        List<Task> tasks = developerService.getTasksByUserStory(storyId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Récupérer toutes mes Tasks")
    public ResponseEntity<List<Task>> getMyTasks(@RequestParam Long developerId) {
        List<Task> tasks = taskService.getTasksByDeveloper(developerId);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/tasks/{taskId}/status")
    @Operation(summary = "Mettre à jour le statut d'une Task")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Status status) {
        Task updated = developerService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/tasks/{taskId}/assign-self")
    @Operation(summary = "S'assigner une Task")
    public ResponseEntity<Task> assignTaskToSelf(
            @PathVariable Long taskId,
            @RequestParam Long developerId) {
        Task updated = developerService.assignTaskToSelf(taskId, developerId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/workload")
    @Operation(summary = "Obtenir ma charge de travail")
    public ResponseEntity<Map<String, Object>> getWorkload(@RequestParam Long developerId) {
        Map<String, Object> workload = developerService.getWorkload(developerId);
        return ResponseEntity.ok(workload);
    }
}