package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import com.project.AgileGestion.service.core.UserStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-stories")
public class UserStoryController {

    private final UserStoryService userStoryService;

    @Autowired
    public UserStoryController(UserStoryService userStoryService) {
        this.userStoryService = userStoryService;
    }

    @PostMapping
    public ResponseEntity<?> createUserStory(@RequestBody UserStory userStory) {
        try {
            UserStory created = userStoryService.createUserStory(userStory);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUserStories() {
        return ResponseEntity.ok(userStoryService.getAllUserStories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserStoryById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userStoryService.getUserStoryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserStory(
            @PathVariable Long id,
            @RequestBody UserStory userStoryDetails) {
        try {
            return ResponseEntity.ok(
                    userStoryService.updateUserStory(id, userStoryDetails)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserStory(@PathVariable Long id) {
        try {
            userStoryService.deleteUserStory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam Status newStatus) {
        try {
            return ResponseEntity.ok(
                    userStoryService.updateStatus(id, newStatus)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable Status status) {
        try {
            return ResponseEntity.ok(
                    userStoryService.getByStatus(status)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/priority-number")
    public ResponseEntity<?> updatePriority(
            @PathVariable Long id,
            @RequestParam Integer newPriority) {
        try {
            return ResponseEntity.ok(
                    userStoryService.updatePriority(id, newPriority)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/priority-level")
    public ResponseEntity<?> updatePriorityLevel(
            @PathVariable Long id,
            @RequestParam PriorityLevel priorityLevel) {
        try {
            return ResponseEntity.ok(
                    userStoryService.updatePriorityLevel(id, priorityLevel)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<?> getByProductBacklog(@PathVariable Long backlogId) {
        return ResponseEntity.ok(
                userStoryService.getByProductBacklog(backlogId)
        );
    }

    @GetMapping("/epic/{epicId}")
    public ResponseEntity<?> getByEpic(@PathVariable Long epicId) {
        return ResponseEntity.ok(
                userStoryService.getByEpic(epicId)
        );
    }

    @PutMapping("/{storyId}/assign-to-epic/{epicId}")
    public ResponseEntity<?> assignToEpic(
            @PathVariable Long storyId,
            @PathVariable Long epicId) {
        try {
            return ResponseEntity.ok(
                    userStoryService.assignToEpic(storyId, epicId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{storyId}/assign-to-backlog/{backlogId}")
    public ResponseEntity<?> assignToProductBacklog(
            @PathVariable Long storyId,
            @PathVariable Long backlogId) {
        try {
            return ResponseEntity.ok(
                    userStoryService.assignToProductBacklog(storyId, backlogId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{storyId}/assign-to-sprint/{sprintBacklogId}")
    public ResponseEntity<?> assignToSprintBacklog(
            @PathVariable Long storyId,
            @PathVariable Long sprintBacklogId) {
        try {
            return ResponseEntity.ok(
                    userStoryService.assignToSprintBacklog(storyId, sprintBacklogId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
