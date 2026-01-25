package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.User;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.entity.enums.Role;
import com.project.AgileGestion.repository.TaskRepository;
import com.project.AgileGestion.repository.UserRepository;
import com.project.AgileGestion.repository.UserStoryRepository;
import com.project.AgileGestion.repository.SprintBacklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserStoryRepository userStoryRepository;
    private final SprintBacklogRepository sprintBacklogRepository;

    //  CRUD BASIQUE

    @Transactional
    public Task createTask(Task task) {
        validateTask(task);
        validateTaskRelations(task);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Task with id %d not found", id)
                ));
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        Task existing = getTaskById(id);
        validateTask(updatedTask);
        validateTaskRelations(updatedTask);

        updateTaskFields(existing, updatedTask);
        return taskRepository.save(existing);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Cannot delete: Task with id %d not found", id)
            );
        }
        taskRepository.deleteById(id);
    }

    //LOGIQUE MÉTIER

    @Transactional
    public Task assignTaskToDeveloper(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        User developer = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("User with id %d not found", userId)
                ));

        if (developer.getRole() != Role.DEVELOPER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only users with DEVELOPER role can be assigned tasks"
            );
        }

        task.setAssignedToUserId(userId);
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, Status newStatus) {
        Task task = getTaskById(taskId);

        // Logique métier pour le changement de statut
        task.setStatus(newStatus);

        if (newStatus == Status.DONE) {
            task.setDateCompletion(LocalDate.now());
        } else if (newStatus == Status.TODO || newStatus == Status.IN_PROGRESS) {
            // Si on repasse à TODO ou IN_PROGRESS, on peut réinitialiser la date de completion
            task.setDateCompletion(null);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task assignTaskWithValidation(Long taskId, Long userId, Role requiredRole) {
        Task task = getTaskById(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("User with id %d not found", userId)
                ));

        if (user.getRole() != requiredRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Only users with %s role can be assigned", requiredRole)
            );
        }

        task.setAssignedToUserId(userId);
        return taskRepository.save(task);
    }

    @Transactional
    public Task moveTaskToSprint(Long taskId, Long sprintBacklogId) {
        Task task = getTaskById(taskId);

        if (sprintBacklogId == null) {
            // Si sprintBacklogId est null, retirer la tâche du sprint
            task.setSprintBacklog(null);
        } else {
            // Vérifier que le sprint existe
            SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("SprintBacklog with id %d not found", sprintBacklogId)
                    ));
            task.setSprintBacklog(sprintBacklog);
        }

        return taskRepository.save(task);
    }

    //  MÉTHODES DE VALIDATION

    private void validateTask(Task task) {
        if (task.getTitre() == null || task.getTitre().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task title is required"
            );
        }

        if (task.getEstimationHeures() != null && task.getEstimationHeures() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estimation hours cannot be negative"
            );
        }
    }

    private void validateTaskRelations(Task task) {
        // Vérifier que la UserStory existe
        if (task.getUserStoryId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userStoryId is required"
            );
        }

        if (!userStoryRepository.existsById(task.getUserStoryId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("UserStory with id %d not found", task.getUserStoryId())
            );
        }

        // Vérifier que le SprintBacklog existe si fourni
        if (task.getSprintBacklogId() != null &&
                !sprintBacklogRepository.existsById(task.getSprintBacklogId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("SprintBacklog with id %d not found", task.getSprintBacklogId())
            );
        }

        // Vérifier que l'utilisateur existe si fourni
        if (task.getAssignedToUserId() != null &&
                !userRepository.existsById(task.getAssignedToUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("User with id %d not found", task.getAssignedToUserId())
            );
        }
    }

    private void updateTaskFields(Task existing, Task updated) {
        if (updated.getTitre() != null) existing.setTitre(updated.getTitre());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getEstimationHeures() != null) existing.setEstimationHeures(updated.getEstimationHeures());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getUserStoryId() != null) existing.setUserStoryId(updated.getUserStoryId());
        if (updated.getSprintBacklog() != null) existing.setSprintBacklog(updated.getSprintBacklog());
        if (updated.getAssignedToUserId() != null) existing.setAssignedToUserId(updated.getAssignedToUserId());

        // Gestion spéciale pour dateCompletion
        if (updated.getStatus() == Status.DONE && existing.getDateCompletion() == null) {
            existing.setDateCompletion(LocalDate.now());
        } else if (updated.getStatus() != Status.DONE && updated.getStatus() != null) {
            existing.setDateCompletion(null);
        }
    }

    //  MÉTHODES DE REQUÊTE

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Status status) {
        return taskRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByUserStory(Long userStoryId) {
        return taskRepository.findByUserStoryId(userStoryId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByDeveloper(Long userId) {
        // Maintenant on utilise assignedToUserId
        return taskRepository.findByAssignedToUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksBySprint(Long sprintBacklogId) {
        return taskRepository.findBySprintBacklogId(sprintBacklogId);
    }

    // MÉTHODES DE CALCUL

    public long getTempsEcoule(Long taskId) {
        Task task = getTaskById(taskId);
        LocalDate startDate = task.getDateCreation();
        LocalDate endDate = task.getDateCompletion() != null ?
                task.getDateCompletion() : LocalDate.now();

        return Math.max(0, ChronoUnit.DAYS.between(startDate, endDate));
    }

    public double getTaskCompletionRate(Long userStoryId) {
        List<Task> tasks = getTasksByUserStory(userStoryId);
        if (tasks.isEmpty()) return 0.0;

        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == Status.DONE)
                .count();

        return (completedTasks * 100.0) / tasks.size();
    }

    public boolean isTaskOverdue(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getDateCompletion() != null || task.getEstimationHeures() == null) {
            return false;
        }

        LocalDate expectedCompletion = task.getDateCreation().plusDays(
                (long) Math.ceil(task.getEstimationHeures() / 8.0)
        );
        return LocalDate.now().isAfter(expectedCompletion);
    }



    @Transactional
    public Task reassignTask(Long taskId, Long newUserId) {
        Task task = getTaskById(taskId);

        // Logique métier pour la réassignation
        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("User with id %d not found", newUserId)
                ));

        if (newUser.getRole() != Role.DEVELOPER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Can only reassign tasks to DEVELOPER role"
            );
        }

        task.setAssignedToUserId(newUserId);
        return taskRepository.save(task);
    }

    @Transactional
    public Task completeTask(Long taskId, String completionNotes) {
        Task task = getTaskById(taskId);

        // Logique métier pour compléter une tâche
        if (task.getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only tasks IN_PROGRESS can be completed"
            );
        }

        task.setStatus(Status.DONE);
        task.setDateCompletion(LocalDate.now());



        return taskRepository.save(task);
    }

    @Transactional
    public Task startTask(Long taskId) {
        Task task = getTaskById(taskId);

        // Logique métier pour démarrer une tâche
        if (task.getStatus() != Status.TODO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only tasks TODO can be started"
            );
        }

        task.setStatus(Status.IN_PROGRESS);
        return taskRepository.save(task);
    }
}