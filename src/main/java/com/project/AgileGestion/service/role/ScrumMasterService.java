package com.project.AgileGestion.service.role;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.service.core.SprintBacklogService;
import com.project.AgileGestion.service.core.UserStoryService;
import com.project.AgileGestion.service.core.TaskService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrumMasterService {

    private final SprintBacklogService sprintBacklogService;
    private final UserStoryService userStoryService;
    private final TaskService taskService;

    // ========== GESTION DES SPRINTS (4.4) ==========

    @Transactional
    public SprintBacklog createNewSprint(String nom, int dureeJours) {
        SprintBacklog sprint = SprintBacklog.builder()
                .nom(nom)
                .dateDebut(LocalDate.now())
                .dateFin(LocalDate.now().plusDays(dureeJours))
                .dureeJours(dureeJours)
                .status(Status.TODO)
                .build();

        return sprintBacklogService.createSprintBacklog(sprint);
    }

    @Transactional
    public SprintBacklog startSprint(Long sprintId) {
        return sprintBacklogService.updateSprintStatus(sprintId, Status.IN_PROGRESS);
    }

    @Transactional
    public SprintBacklog completeSprint(Long sprintId) {
        return sprintBacklogService.updateSprintStatus(sprintId, Status.DONE);
    }

    @Transactional
    public SprintBacklog updateSprint(Long sprintId, SprintBacklog sprintUpdate) {
        return sprintBacklogService.updateSprintBacklog(sprintId, sprintUpdate);
    }

    @Transactional
    public void deleteSprint(Long sprintId) {
        sprintBacklogService.deleteSprintBacklog(sprintId);
    }

    // ========== SUIVI DE L'AVANCEMENT (8) ==========

    public SprintProgress getSprintProgress(Long sprintId) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);

        long totalStories = sprint.getUserStories().size();
        long completedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE)
                .count();

        // CORRECTION : Récupérer les tâches via TaskService
        long totalTasks = 0;
        long completedTasks = 0;

        for (UserStory userStory : sprint.getUserStories()) {
            List<Task> tasks = taskService.getTasksByUserStory(userStory.getId());
            totalTasks += tasks.size();
            completedTasks += tasks.stream()
                    .filter(task -> task.getStatus() == Status.DONE)
                    .count();
        }

        double velocity = sprintBacklogService.calculateVelocite(sprintId);
        int remainingDays = sprintBacklogService.calculateRemainingDays(sprintId);

        return SprintProgress.builder()
                .sprintName(sprint.getNom())
                .totalStories(totalStories)
                .completedStories(completedStories)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .velocity(velocity)
                .remainingDays(remainingDays)
                .completionPercentage(totalStories > 0 ?
                        (int) ((completedStories * 100) / totalStories) : 0)
                .build();
    }

    // ========== GESTION DES USER STORIES DANS LE SPRINT (4.4) ==========

    @Transactional
    public void addUserStoryToSprint(Long sprintId, Long userStoryId) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);
        UserStory userStory = userStoryService.getUserStoryById(userStoryId);

        // Vérifier si la story est prête
        if (userStory.getStatut() == Status.BLOCKED) {
            throw new IllegalStateException("Impossible d'ajouter une story bloquée au sprint");
        }

        // Ajouter la story au sprint
        sprint.getUserStories().add(userStory);
        sprintBacklogService.updateSprintBacklog(sprintId, sprint);
    }

    @Transactional
    public void removeUserStoryFromSprint(Long sprintId, Long userStoryId) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);

        // Retirer la story du sprint
        sprint.getUserStories().removeIf(us -> us.getId().equals(userStoryId));
        sprintBacklogService.updateSprintBacklog(sprintId, sprint);
    }

    // ========== SUIVI QUOTIDIEN (4.4) ==========

    public DailyStandupReport generateDailyStandupReport(Long sprintId) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);

        // CORRECTION : Récupérer les stories complétées hier via les tâches
        List<UserStory> completedYesterday = new ArrayList<>();
        for (UserStory userStory : sprint.getUserStories()) {
            if (userStory.getStatut() == Status.DONE) {
                // Vérifier la date de dernière tâche complétée
                List<Task> tasks = taskService.getTasksByUserStory(userStory.getId());
                boolean completedYesterdayFlag = tasks.stream()
                        .filter(task -> task.getStatus() == Status.DONE)
                        .anyMatch(task -> task.getDateCompletion() != null &&
                                task.getDateCompletion().equals(LocalDate.now().minusDays(1)));

                if (completedYesterdayFlag) {
                    completedYesterday.add(userStory);
                }
            }
        }

        // Stories en cours
        List<UserStory> inProgress = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.IN_PROGRESS)
                .toList();

        // Blocages
        List<UserStory> blockedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.BLOCKED)
                .toList();

        return DailyStandupReport.builder()
                .date(LocalDate.now())
                .sprintName(sprint.getNom())
                .completedYesterday(completedYesterday.size())
                .inProgressCount(inProgress.size())
                .blockedCount(blockedStories.size())
                .remainingDays(sprintBacklogService.calculateRemainingDays(sprintId))
                .velocity(sprintBacklogService.calculateVelocite(sprintId))
                .progress(sprintBacklogService.calculateProgress(sprintId))
                .blockedStoriesDetails(blockedStories.stream()
                        .map(us -> us.getTitre() + " (ID: " + us.getId() + ")")
                        .toList())
                .build();
    }

    // ========== GESTION DES RÉTROSPECTIVES (8) ==========

    public RetrospectiveReport generateRetrospectiveReport(Long sprintId) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);

        double plannedVelocity = sprint.getUserStories().stream()
                .mapToInt(UserStory::getStoryPoints)
                .sum();

        double actualVelocity = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE)
                .mapToInt(UserStory::getStoryPoints)
                .sum();

        // Calcul des métriques de rétrospective
        double velocityDifference = actualVelocity - plannedVelocity;
        double accuracy = plannedVelocity > 0 ? (actualVelocity / plannedVelocity) * 100 : 0;

        // Identification des problèmes
        long blockedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.BLOCKED)
                .count();

        // CORRECTION : Calculer les stories terminées en retard
        long storiesCompletedLate = 0;
        for (UserStory userStory : sprint.getUserStories()) {
            if (userStory.getStatut() == Status.DONE) {
                // Vérifier si la dernière tâche a été complétée après la fin du sprint
                List<Task> tasks = taskService.getTasksByUserStory(userStory.getId());
                LocalDate latestCompletion = tasks.stream()
                        .filter(task -> task.getDateCompletion() != null)
                        .map(Task::getDateCompletion)
                        .max(LocalDate::compareTo)
                        .orElse(null);

                if (latestCompletion != null && latestCompletion.isAfter(sprint.getDateFin())) {
                    storiesCompletedLate++;
                }
            }
        }

        return RetrospectiveReport.builder()
                .sprintName(sprint.getNom())
                .plannedVelocity(plannedVelocity)
                .actualVelocity(actualVelocity)
                .velocityDifference(velocityDifference)
                .accuracyPercentage(accuracy)
                .blockedStories(blockedStories)
                .storiesCompletedLate(storiesCompletedLate)
                .recommendations(generateRetrospectiveRecommendations(
                        velocityDifference, blockedStories, storiesCompletedLate))
                .build();
    }

    // ========== MÉTHODES UTILITAIRES ==========

    public List<UserStory> getSprintBacklog(Long sprintId) {
        return sprintBacklogService.getUserStoriesInSprint(sprintId);
    }

    public void updateSprintName(Long sprintId, String newName) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);
        sprint.setNom(newName);
        sprintBacklogService.updateSprintBacklog(sprintId, sprint);
    }

    public void updateSprintDates(Long sprintId, LocalDate startDate, LocalDate endDate) {
        SprintBacklog sprint = sprintBacklogService.getSprintBacklogById(sprintId);
        sprint.setDateDebut(startDate);
        sprint.setDateFin(endDate);
        sprintBacklogService.updateSprintBacklog(sprintId, sprint);
    }

    // ========== RECORDS ==========

    @Builder
    public record SprintProgress(
            String sprintName,
            long totalStories,
            long completedStories,
            long totalTasks,
            long completedTasks,
            double velocity,
            int remainingDays,
            int completionPercentage
    ) {}

    @Builder
    public record DailyStandupReport(
            LocalDate date,
            String sprintName,
            int completedYesterday,
            int inProgressCount,
            int blockedCount,
            int remainingDays,
            double velocity,
            double progress,
            List<String> blockedStoriesDetails
    ) {}

    @Builder
    public record RetrospectiveReport(
            String sprintName,
            double plannedVelocity,
            double actualVelocity,
            double velocityDifference,
            double accuracyPercentage,
            long blockedStories,
            long storiesCompletedLate,
            List<String> recommendations
    ) {}

    // ========== MÉTHODES PRIVÉES ==========

    private List<String> generateRetrospectiveRecommendations(
            double velocityDifference,
            long blockedStories,
            long storiesCompletedLate) {

        List<String> recommendations = new ArrayList<>();

        if (velocityDifference < 0) {
            recommendations.add("📉 Vélocité inférieure de " + Math.abs(velocityDifference) +
                    " points. Évaluez la complexité des stories.");
        }

        if (blockedStories > 0) {
            recommendations.add("🚧 " + blockedStories + " stories bloquées. " +
                    "Identifiez et résolvez les blocages rapidement.");
        }

        if (storiesCompletedLate > 0) {
            recommendations.add("⏰ " + storiesCompletedLate + " stories terminées en retard. " +
                    "Améliorez l'estimation des délais.");
        }

        if (velocityDifference > 0 && velocityDifference > 5) {
            recommendations.add("🎯 Vélocité supérieure de " + velocityDifference +
                    " points. Capacité de l'équipe sous-estimée.");
        }

        return recommendations;
    }
}