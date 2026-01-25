package com.project.AgileGestion.service.core.prioritization;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.Task;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.repository.SprintBacklogRepository;
import com.project.AgileGestion.repository.UserStoryRepository;
import com.project.AgileGestion.repository.TaskRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final SprintBacklogRepository sprintBacklogRepository;
    private final UserStoryRepository userStoryRepository;
    private final TaskRepository taskRepository;

    // Pattern: Strategy pour différents types de calculs
    public interface MetricCalculator {
        double calculate(List<SprintBacklog> sprints);
    }

    // Calcul de vélocité moyenne
    public double calculateTeamVelocity() {
        List<SprintBacklog> completedSprints = sprintBacklogRepository
                .findByStatus(Status.DONE);

        if (completedSprints.isEmpty()) {
            return 0.0;
        }

        double totalStoryPoints = completedSprints.stream()
                .mapToDouble(this::calculateSprintStoryPoints)
                .sum();

        return totalStoryPoints / completedSprints.size();
    }

    // Calcul du taux de complétion
    public double calculateSprintCompletionRate(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Sprint with id %d not found", sprintId)
                ));

        long totalStories = sprint.getUserStories().size();
        if (totalStories == 0) {
            return 0.0;
        }

        long completedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut() au lieu de getStatus()
                .count();

        return (completedStories * 100.0) / totalStories;
    }

    // Pattern: Composite pour les statistiques multiples
    public Map<String, Object> getProjectStatistics() {
        return Map.of(
                "teamVelocity", calculateTeamVelocity(),
                "totalUserStories", countUserStories(),
                "completedUserStories", countCompletedUserStories(),
                "totalTasks", countTasks(),
                "completedTasks", countCompletedTasks(),
                "averageLeadTime", calculateAverageLeadTime(),
                "onTimeDeliveryRate", calculateOnTimeDeliveryRate()
        );
    }

    // Calcul du lead time moyen - CORRIGÉ
    public double calculateAverageLeadTime() {
        List<UserStory> completedStories = userStoryRepository.findAll().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                .collect(Collectors.toList());

        if (completedStories.isEmpty()) {
            return 0.0;
        }

        double totalLeadTime = completedStories.stream()
                .mapToDouble(this::calculateUserStoryLeadTime)
                .sum();

        return totalLeadTime / completedStories.size();
    }

    // Calcul du taux de livraison dans les temps
    public double calculateOnTimeDeliveryRate() {
        List<SprintBacklog> completedSprints = sprintBacklogRepository
                .findByStatus(Status.DONE);

        if (completedSprints.isEmpty()) {
            return 0.0;
        }

        long onTimeSprints = completedSprints.stream()
                .filter(this::isSprintOnTime)
                .count();

        return (onTimeSprints * 100.0) / completedSprints.size();
    }

    // Pattern: Builder pour les rapports détaillés
    public SprintReport generateSprintReport(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Sprint with id %d not found", sprintId)
                ));

        return SprintReport.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getNom())
                .startDate(sprint.getDateDebut())
                .endDate(sprint.getDateFin())
                .status(sprint.getStatus())
                .totalStories(sprint.getUserStories().size())
                .completedStories(countCompletedStoriesInSprint(sprint))
                .totalStoryPoints(calculateSprintStoryPoints(sprint))
                .completedStoryPoints(calculateCompletedStoryPoints(sprint))
                .velocity(calculateSprintVelocity(sprint))
                .completionRate(calculateSprintCompletionRate(sprintId))
                .build();
    }

    // Méthodes utilitaires
    private double calculateSprintStoryPoints(SprintBacklog sprint) {
        return sprint.getUserStories().stream()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    private double calculateCompletedStoryPoints(SprintBacklog sprint) {
        return sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    private double calculateSprintVelocity(SprintBacklog sprint) {
        return sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    private long countCompletedStoriesInSprint(SprintBacklog sprint) {
        return sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                .count();
    }

    private long countUserStories() {
        return userStoryRepository.count();
    }

    private long countCompletedUserStories() {
        return userStoryRepository.findAll().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                .count();
    }

    private long countTasks() {
        return taskRepository.count();
    }

    private long countCompletedTasks() {
        return taskRepository.findByStatus(Status.DONE).size();
    }

    // CORRECTION : Calcul du lead time d'une UserStory
    private double calculateUserStoryLeadTime(UserStory userStory) {
        // Recherche des tâches associées à cette UserStory
        List<Task> tasks = taskRepository.findByUserStoryId(userStory.getId());

        if (tasks.isEmpty()) {
            return 0.0;
        }

        Optional<LocalDate> startDate = tasks.stream()
                .map(Task::getDateCreation)
                .min(LocalDate::compareTo); // CORRIGÉ : Utiliser LocalDate::compareTo

        Optional<LocalDate> endDate = tasks.stream()
                .map(Task::getDateCompletion)
                .filter(date -> date != null)
                .max(LocalDate::compareTo); // CORRIGÉ : Utiliser LocalDate::compareTo

        if (startDate.isPresent() && endDate.isPresent()) {
            return ChronoUnit.DAYS.between(startDate.get(), endDate.get());
        }

        return 0.0;
    }

    // CORRECTION : Vérification si un sprint est dans les temps
    private boolean isSprintOnTime(SprintBacklog sprint) {
        if (sprint.getDateFin() == null) {
            return false;
        }

        // Date de fin la plus tardive parmi toutes les tâches du sprint
        LocalDate latestCompletionDate = sprint.getUserStories().stream()
                .flatMap(us -> taskRepository.findByUserStoryId(us.getId()).stream())
                .map(Task::getDateCompletion)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (latestCompletionDate == null) {
            return false;
        }

        return !latestCompletionDate.isAfter(sprint.getDateFin());
    }

    // NOUVELLES MÉTHODES UTILES
    public BurndownChartData generateBurndownChart(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Sprint with id %d not found", sprintId)
                ));

        int totalStoryPoints = sprint.getUserStories().stream()
                .mapToInt(UserStory::getStoryPoints)
                .sum();

        LocalDate today = LocalDate.now();
        int daysPassed = (int) ChronoUnit.DAYS.between(
                sprint.getDateDebut(),
                today.isAfter(sprint.getDateFin()) ? sprint.getDateFin() : today
        );

        int totalDays = sprint.getDureeJours() != null ? sprint.getDureeJours() :
                (int) ChronoUnit.DAYS.between(sprint.getDateDebut(), sprint.getDateFin());

        // Calcul idéal (linéaire)
        double idealBurndown = Math.max(0, totalStoryPoints * (1 - ((double) daysPassed / totalDays)));

        // Calcul réel
        int completedStoryPoints = sprint.getUserStories().stream()
                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
        double actualBurndown = Math.max(0, totalStoryPoints - completedStoryPoints);

        return BurndownChartData.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getNom())
                .totalStoryPoints(totalStoryPoints)
                .idealBurndown(idealBurndown)
                .actualBurndown(actualBurndown)
                .daysPassed(daysPassed)
                .totalDays(totalDays)
                .remainingDays(Math.max(0, totalDays - daysPassed))
                .build();
    }

    // Calcul de la capacité de l'équipe
    public TeamCapacity calculateTeamCapacity(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Sprint with id %d not found", sprintId)
                ));

        // Calculer le nombre total d'heures de travail
        int totalEstimatedHours = sprint.getUserStories().stream()
                .flatMap(us -> taskRepository.findByUserStoryId(us.getId()).stream())
                .mapToInt(task -> task.getEstimationHeures() != null ? task.getEstimationHeures() : 0)
                .sum();

        // Calculer les heures travaillées
        int workedHours = sprint.getUserStories().stream()
                .flatMap(us -> taskRepository.findByUserStoryId(us.getId()).stream())
                .filter(task -> task.getStatus() == Status.DONE)
                .mapToInt(task -> task.getEstimationHeures() != null ? task.getEstimationHeures() : 0)
                .sum();

        int remainingHours = totalEstimatedHours - workedHours;
        double capacityUtilization = totalEstimatedHours > 0 ?
                (workedHours * 100.0) / totalEstimatedHours : 0.0;

        return TeamCapacity.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getNom())
                .totalEstimatedHours(totalEstimatedHours)
                .workedHours(workedHours)
                .remainingHours(remainingHours)
                .capacityUtilization(capacityUtilization)
                .build();
    }

    // Pattern: Factory pour les différentes métriques
    public MetricCalculator getVelocityCalculator() {
        return new VelocityCalculator();
    }

    public MetricCalculator getCompletionRateCalculator() {
        return new CompletionRateCalculator();
    }

    // Implémentations concrètes des stratégies
    private class VelocityCalculator implements MetricCalculator {
        @Override
        public double calculate(List<SprintBacklog> sprints) {
            if (sprints.isEmpty()) return 0.0;
            return sprints.stream()
                    .mapToDouble(StatisticsService.this::calculateSprintVelocity)
                    .average()
                    .orElse(0.0);
        }
    }

    private class CompletionRateCalculator implements MetricCalculator {
        @Override
        public double calculate(List<SprintBacklog> sprints) {
            if (sprints.isEmpty()) return 0.0;
            return sprints.stream()
                    .mapToDouble(sprint -> {
                        long total = sprint.getUserStories().size();
                        if (total == 0) return 0.0;
                        long completed = sprint.getUserStories().stream()
                                .filter(us -> us.getStatut() == Status.DONE) // CORRIGÉ : getStatut()
                                .count();
                        return (completed * 100.0) / total;
                    })
                    .average()
                    .orElse(0.0);
        }
    }

    // Records pour les données de suivi - CORRIGÉ
    @Builder
    public static class SprintReport {
        private final Long sprintId;
        private final String sprintName;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Status status;
        private final long totalStories;
        private final long completedStories;
        private final double totalStoryPoints;
        private final double completedStoryPoints;
        private final double velocity;
        private final double completionRate;

        // Lombok génère le builder automatiquement
    }

    @Builder
    public static class BurndownChartData {
        private final Long sprintId;
        private final String sprintName;
        private final int totalStoryPoints;
        private final double idealBurndown;
        private final double actualBurndown;
        private final int daysPassed;
        private final int totalDays;
        private final int remainingDays;
    }

    @Builder
    public static class TeamCapacity {
        private final Long sprintId;
        private final String sprintName;
        private final int totalEstimatedHours;
        private final int workedHours;
        private final int remainingHours;
        private final double capacityUtilization;
    }
}