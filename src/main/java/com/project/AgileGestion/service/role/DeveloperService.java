package com.project.AgileGestion.service.role;

import com.project.AgileGestion.entity.Epic;
import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.User;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.Role;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.repository.UserRepository;
import com.project.AgileGestion.service.core.*;
import com.project.AgileGestion.service.core.prioritization.StoryPrioritizationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service ROLE pour le Développeur
 * Responsabilité : Estimation, cycle de vie des stories, développement
 */
@Service
public class DeveloperService {

    private final UserStoryService userStoryService;
    private final EpicService epicService;
    private final StoryPrioritizationService prioritizationService;
    private final StoryValidator storyValidator;
    private final StoryFormatter storyFormatter;
    private final TaskService taskService;
    private final UserRepository userRepository;

    public DeveloperService(
            UserStoryService userStoryService,
            EpicService epicService,
            StoryPrioritizationService prioritizationService,
            StoryValidator storyValidator,
            StoryFormatter storyFormatter,
            TaskService taskService,
            UserRepository userRepository) {
        this.userStoryService = userStoryService;
        this.epicService = epicService;
        this.prioritizationService = prioritizationService;
        this.storyValidator = storyValidator;
        this.storyFormatter = storyFormatter;
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    // ========== GESTION DU CYCLE DE VIE DES STORIES ==========

    public UserStory startWorkingOnStory(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);

        if (!storyValidator.isReadyForSprint(story)) {
            throw new IllegalStateException("Cette story n'est pas prête pour le développement.");
        }

        if (story.getStatut() == Status.IN_PROGRESS || story.getStatut() == Status.DONE) {
            throw new IllegalStateException("Cette story est déjà " + story.getStatut().name().toLowerCase());
        }

        return userStoryService.updateStatus(storyId, Status.IN_PROGRESS);
    }

    public UserStory completeStory(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);

        if (story.getStatut() != Status.IN_PROGRESS) {
            throw new IllegalStateException("Seules les stories en cours peuvent être marquées comme terminées");
        }

        return userStoryService.updateStatus(storyId, Status.DONE);
    }

    public UserStory markStoryAsBlocked(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);

        if (story.getStatut() != Status.IN_PROGRESS) {
            throw new IllegalStateException("Seules les stories en cours peuvent être bloquées");
        }

        return userStoryService.updateStatus(storyId, Status.BLOCKED);
    }

    // ========== VISUALISATION DES TÂCHES ==========

    public List<UserStory> getMyStoriesInBacklog(Long backlogId) {
        return userStoryService.getByProductBacklog(backlogId).stream()
                .filter(story -> story.getStatut() == Status.TODO ||
                        story.getStatut() == Status.IN_PROGRESS)
                .collect(Collectors.toList());
    }

    public List<UserStory> getMyCompletedStories(Long backlogId) {
        return userStoryService.getByProductBacklog(backlogId).stream()
                .filter(story -> story.getStatut() == Status.DONE)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getWorkSummary(Long backlogId) {
        List<UserStory> allStories = userStoryService.getByProductBacklog(backlogId);

        List<UserStory> todoStories = allStories.stream()
                .filter(story -> story.getStatut() == Status.TODO)
                .toList();

        List<UserStory> inProgressStories = allStories.stream()
                .filter(story -> story.getStatut() == Status.IN_PROGRESS)
                .toList();

        List<UserStory> doneStories = allStories.stream()
                .filter(story -> story.getStatut() == Status.DONE)
                .toList();

        int totalPoints = allStories.stream()
                .mapToInt(story -> story.getStoryPoints() != null ? story.getStoryPoints() : 0)
                .sum();

        int donePoints = doneStories.stream()
                .mapToInt(story -> story.getStoryPoints() != null ? story.getStoryPoints() : 0)
                .sum();

        double completionPercentage = totalPoints > 0 ?
                ((double) donePoints / totalPoints) * 100 : 0;

        return Map.of(
                "totalStories", allStories.size(),
                "todoStories", todoStories.size(),
                "inProgressStories", inProgressStories.size(),
                "doneStories", doneStories.size(),
                "totalPoints", totalPoints,
                "donePoints", donePoints,
                "completionPercentage", String.format("%.1f%%", completionPercentage),
                "remainingPoints", totalPoints - donePoints
        );
    }

    // ========== OUTILS DE DÉVELOPPEMENT ==========

    public Map<String, Object> getStoryDetailsForDevelopment(Long storyId) {
        UserStory story = userStoryService.getUserStoryById(storyId);

        Map<String, Object> details = new HashMap<>();
        details.put("id", story.getId());
        details.put("titre", story.getTitre());
        details.put("description", story.getDescription());
        details.put("formattedStory", storyFormatter.formatAsStandard(story));
        details.put("acceptanceCriteria", story.getCriteresAcceptation());
        details.put("storyPoints", story.getStoryPoints());
        details.put("priority", story.getPriorityLevel().name());
        details.put("status", story.getStatut().name());
        details.put("isReadyForDev", storyValidator.isReadyForSprint(story));

        // Informations Epic (optionnel pour contexte)
        if (story.getEpicId() != null) {
            try {
                Epic epic = epicService.getEpicById(story.getEpicId());
                details.put("epicTitle", epic.getTitre());
                details.put("epicDescription", epic.getDescription());
            } catch (Exception e) {
                // Epic non trouvé
            }
        }
        return details;
    }

    // 4.6.1 - Créer une Task pour une User Story
    public Task createTaskForStory(Long storyId, Task task) {
        UserStory story = userStoryService.getUserStoryById(storyId);
        task.setUserStoryId(storyId);
        task.setUserStory(story);
        task.setDateCreation(LocalDate.now());
        task.setStatus(Status.TODO);
        return taskService.createTask(task);
    }

    // 4.6.2 - Récupérer les Tasks d'une User Story
    public List<Task> getTasksByUserStory(Long storyId) {
        return taskService.getTasksByUserStory(storyId);
    }

    // 4.6.3 - Mettre à jour le statut d'une Task
    public Task updateTaskStatus(Long taskId, Status status) {
        Task task = taskService.getTaskById(taskId);

        if (status == Status.DONE) {
            task.setDateCompletion(LocalDate.now());
        }

        return taskService.updateTaskStatus(taskId, status);
    }

    // 4.6.4 - Assigner une Task à soi-même
    public Task assignTaskToSelf(Long taskId, Long developerId) {
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Développeur non trouvé"));

        if (developer.getRole() != Role.DEVELOPER) {
            throw new IllegalStateException("Seuls les développeurs peuvent s'assigner des tâches");
        }

        return taskService.assignTaskToDeveloper(taskId, developerId);
    }

    // 4.6.5 - Calculer la charge de travail
    public Map<String, Object> getWorkload(Long developerId) {
        List<Task> tasks = taskService.getTasksByDeveloper(developerId);

        long todoTasks = tasks.stream().filter(t -> t.getStatus() == Status.TODO).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
        long doneTasks = tasks.stream().filter(t -> t.getStatus() == Status.DONE).count();

        int totalHours = tasks.stream()
                .mapToInt(t -> t.getEstimationHeures() != null ? t.getEstimationHeures() : 0)
                .sum();

        return Map.of(
                "totalTasks", tasks.size(),
                "todoTasks", todoTasks,
                "inProgressTasks", inProgressTasks,
                "doneTasks", doneTasks,
                "totalEstimatedHours", totalHours,
                "completionRate", tasks.isEmpty() ? 0 : (doneTasks * 100.0 / tasks.size())
        );
    }

    // 4.6.6 - Marquer toutes les Tasks comme DONE quand la Story est complétée
    public void completeAllTasksForStory(Long storyId) {
        List<Task> tasks = taskService.getTasksByUserStory(storyId);
        tasks.forEach(task -> {
            if (task.getStatus() != Status.DONE) {
                task.setStatus(Status.DONE);
                task.setDateCompletion(LocalDate.now());
                taskService.updateTask(task.getId(), task);
            }
        });
    }
}
