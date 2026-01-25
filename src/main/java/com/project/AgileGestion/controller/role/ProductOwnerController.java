package com.project.AgileGestion.controller.role;

import com.project.AgileGestion.entity.Epic;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.PrioritizationMethod;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import com.project.AgileGestion.service.role.ProductOwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour les fonctionnalités du Product Owner
 */
@RestController
@RequestMapping("/api/product-owner")
public class ProductOwnerController {

    private final ProductOwnerService productOwnerService;

    @Autowired
    public ProductOwnerController(ProductOwnerService productOwnerService) {
        this.productOwnerService = productOwnerService;
    }

    // ========== GESTION DES PRIORITÉS ==========

    @PutMapping("/stories/{storyId}/priority-level")
    public ResponseEntity<?> updateStoryPriority(
            @PathVariable Long storyId,
            @RequestBody PriorityLevel priorityLevel) {
        try {
            UserStory updatedStory = productOwnerService.updateStoryPriority(storyId, priorityLevel);
            return ResponseEntity.ok(updatedStory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données invalides", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour de la priorité", "message", e.getMessage()));
        }
    }

    @PutMapping("/stories/{storyId}/priority-number")
    public ResponseEntity<?> updatePriorityNumber(
            @PathVariable Long storyId,
            @RequestBody Integer priorityNumber) {
        try {
            UserStory updatedStory = productOwnerService.updatePriorityNumber(storyId, priorityNumber);
            return ResponseEntity.ok(updatedStory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Numéro de priorité invalide", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour du numéro de priorité", "message", e.getMessage()));
        }
    }

    // ========== PRIORISATION DU BACKLOG ==========

    @GetMapping("/backlogs/{backlogId}/prioritized")
    public ResponseEntity<?> getPrioritizedBacklog(
            @PathVariable Long backlogId,
            @RequestParam PrioritizationMethod method) {
        try {
            List<UserStory> prioritizedStories = productOwnerService.getPrioritizedBacklog(backlogId, method);
            return ResponseEntity.ok(prioritizedStories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Méthode de priorisation invalide", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la priorisation du backlog", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlogs/{backlogId}/sorted")
    public ResponseEntity<?> getSortedByPriority(@PathVariable Long backlogId) {
        try {
            List<UserStory> sortedStories = productOwnerService.getSortedByPriority(backlogId);
            return ResponseEntity.ok(sortedStories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du tri du backlog", "message", e.getMessage()));
        }
    }

    // ========== DASHBOARDS ET RAPPORTS ==========

    @GetMapping("/backlogs/{backlogId}/dashboard")
    public ResponseEntity<?> getBacklogDashboard(@PathVariable Long backlogId) {
        try {
            Map<String, Object> dashboard = productOwnerService.getBacklogDashboard(backlogId);
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du dashboard", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlogs/{backlogId}/report")
    public ResponseEntity<?> generateReport(@PathVariable Long backlogId) {
        try {
            Map<String, Object> report = productOwnerService.generateReport(backlogId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du rapport", "message", e.getMessage()));
        }
    }

    // ========== GESTION DES EPICS ==========

    @PostMapping("/epics")
    public ResponseEntity<?> createEpic(@RequestBody Epic epic) {
        try {
            Epic createdEpic = productOwnerService.createEpic(epic);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEpic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données de l'epic invalides", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de l'epic", "message", e.getMessage()));
        }
    }

    @PostMapping("/epics/{epicId}/organize")
    public ResponseEntity<?> organizeStoriesIntoEpic(
            @PathVariable Long epicId,
            @RequestBody List<Long> storyIds) {
        try {
            productOwnerService.organizeStoriesIntoEpic(epicId, storyIds);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données invalides", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'organisation des user stories", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlogs/{backlogId}/epics")
    public ResponseEntity<?> getEpicsForBacklog(@PathVariable Long backlogId) {
        try {
            List<Epic> epics = productOwnerService.getEpicsForBacklog(backlogId);
            return ResponseEntity.ok(epics);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des epics", "message", e.getMessage()));
        }
    }

    @GetMapping("/epics/{epicId}/stories")
    public ResponseEntity<?> getStoriesForEpic(@PathVariable Long epicId) {
        try {
            List<UserStory> stories = productOwnerService.getStoriesForEpic(epicId);
            return ResponseEntity.ok(stories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des user stories", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlogs/{backlogId}/organization")
    public ResponseEntity<?> getBacklogOrganization(@PathVariable Long backlogId) {
        try {
            Map<String, Object> organization = productOwnerService.getBacklogOrganization(backlogId);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'organisation", "message", e.getMessage()));
        }
    }

    // ========== RECOMMANDATIONS ==========

    @GetMapping("/backlogs/{backlogId}/recommendations")
    public ResponseEntity<?> getPrioritizationRecommendations(@PathVariable Long backlogId) {
        try {
            List<String> recommendations = productOwnerService.getPrioritizationRecommendations(backlogId);
            return ResponseEntity.ok(recommendations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération des recommandations", "message", e.getMessage()));
        }
    }
}