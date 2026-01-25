package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.Epic;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.service.core.EpicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/epics")
public class EpicController {

    private final EpicService epicService;

    @Autowired
    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    @PostMapping
    public ResponseEntity<?> createEpic(@RequestBody Epic epic) {
        try {
            Epic created = epicService.createEpic(epic);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données de l'epic invalides", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ressource non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de l'epic", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllEpics() {
        try {
            List<Epic> epics = epicService.getAllEpics();
            return ResponseEntity.ok(epics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des epics", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEpicById(@PathVariable Long id) {
        try {
            Epic epic = epicService.getEpicById(id);
            return ResponseEntity.ok(epic);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'epic", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/with-stories")
    public ResponseEntity<?> getEpicWithUserStories(@PathVariable Long id) {
        try {
            Epic epic = epicService.getEpicWithUserStories(id);
            return ResponseEntity.ok(epic);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'epic avec user stories", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEpic(
            @PathVariable Long id,
            @RequestBody Epic epicDetails) {
        try {
            Epic updated = epicService.updateEpic(id, epicDetails);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données de l'epic invalides", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour de l'epic", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEpic(@PathVariable Long id) {
        try {
            epicService.deleteEpic(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression de l'epic", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<?> getEpicsByBacklogId(@PathVariable Long backlogId) {
        try {
            List<Epic> epics = epicService.getEpicsByBacklogId(backlogId);
            return ResponseEntity.ok(epics);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des epics", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/search")
    public ResponseEntity<?> searchEpicsByTitle(
            @PathVariable Long backlogId,
            @RequestParam(required = false) String keyword) {
        try {
            List<Epic> epics = epicService.searchEpicsByTitle(backlogId, keyword);
            return ResponseEntity.ok(epics);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la recherche des epics", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/count")
    public ResponseEntity<?> countEpicsByBacklogId(@PathVariable Long backlogId) {
        try {
            Long count = epicService.countEpicsByBacklogId(backlogId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du comptage des epics", "message", e.getMessage()));
        }
    }

    @GetMapping("/{epicId}/check-backlog/{backlogId}")
    public ResponseEntity<?> isEpicInBacklog(
            @PathVariable Long epicId,
            @PathVariable Long backlogId) {
        try {
            boolean exists = epicService.isEpicInBacklog(epicId, backlogId);
            return ResponseEntity.ok(exists);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic ou backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la vérification", "message", e.getMessage()));
        }
    }

    @PostMapping("/{epicId}/associate-story/{storyId}")
    public ResponseEntity<?> associateUserStoryToEpic(
            @PathVariable Long epicId,
            @PathVariable Long storyId) {
        try {
            epicService.associateUserStoryToEpic(epicId, storyId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données invalides", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic ou user story non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'association", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/detach-story/{storyId}")
    public ResponseEntity<?> detachUserStoryFromEpic(@PathVariable Long storyId) {
        try {
            epicService.detachUserStoryFromEpic(storyId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User story non trouvée", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la dissociation", "message", e.getMessage()));
        }
    }

    @PostMapping("/{epicId}/associate-multiple")
    public ResponseEntity<?> associateMultipleUserStoriesToEpic(
            @PathVariable Long epicId,
            @RequestBody List<Long> storyIds) {
        try {
            epicService.associateMultipleUserStoriesToEpic(epicId, storyIds);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Liste de user stories invalide", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'association multiple", "message", e.getMessage()));
        }
    }

    @GetMapping("/{epicId}/stories")
    public ResponseEntity<?> getUserStoriesForEpic(@PathVariable Long epicId) {
        try {
            List<UserStory> stories = epicService.getUserStoriesForEpic(epicId);
            return ResponseEntity.ok(stories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des user stories", "message", e.getMessage()));
        }
    }

    @PostMapping("/transfer-stories")
    public ResponseEntity<?> transferUserStories(
            @RequestParam Long fromEpicId,
            @RequestParam Long toEpicId,
            @RequestBody List<Long> userStoryIds) {
        try {
            epicService.transferUserStories(fromEpicId, toEpicId, userStoryIds);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données de transfert invalides", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic source ou destination non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du transfert des user stories", "message", e.getMessage()));
        }
    }

    @GetMapping("/{epicId}/statistics")
    public ResponseEntity<?> getEpicStatistics(@PathVariable Long epicId) {
        try {
            Map<String, Object> stats = epicService.getEpicStatistics(epicId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération des statistiques", "message", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}/statistics")
    public ResponseEntity<?> getBacklogEpicsStatistics(@PathVariable Long backlogId) {
        try {
            Map<String, Object> stats = epicService.getBacklogEpicsStatistics(backlogId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération des statistiques des epics", "message", e.getMessage()));
        }
    }

    @GetMapping("/{epicId}/can-delete")
    public ResponseEntity<?> canDeleteEpic(@PathVariable Long epicId) {
        try {
            boolean canDelete = epicService.canDeleteEpic(epicId);
            return ResponseEntity.ok(canDelete);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Epic non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la vérification de suppression", "message", e.getMessage()));
        }
    }
}