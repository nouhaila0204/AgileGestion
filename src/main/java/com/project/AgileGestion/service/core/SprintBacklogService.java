package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.Status;
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
public class SprintBacklogService {

    private final SprintBacklogRepository sprintBacklogRepository;

    // Pattern: Factory Method pour créer des sprints avec validation
    @Transactional
    public SprintBacklog createSprintBacklog(SprintBacklog sprintBacklog) {
        validateSprintDates(sprintBacklog);
        return sprintBacklogRepository.save(sprintBacklog);
    }

    @Transactional(readOnly = true)
    public SprintBacklog getSprintBacklogById(Long id) {
        return sprintBacklogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("SprintBacklog with id %d not found", id)
                ));
    }

    @Transactional
    public SprintBacklog updateSprintBacklog(Long id, SprintBacklog updatedSprint) {
        SprintBacklog existing = getSprintBacklogById(id);
        validateSprintDates(updatedSprint);

        // Pattern: Builder pour la mise à jour partielle
        updateSprintFields(existing, updatedSprint);
        return sprintBacklogRepository.save(existing);
    }

    @Transactional
    public void deleteSprintBacklog(Long id) {
        if (!sprintBacklogRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Cannot delete: SprintBacklog with id %d not found", id)
            );
        }
        sprintBacklogRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SprintBacklog> getAllSprintBacklogs() {
        return sprintBacklogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SprintBacklog> getActiveSprints() {
        return sprintBacklogRepository.findByStatus(Status.IN_PROGRESS);
    }

    @Transactional(readOnly = true)
    public List<UserStory> getUserStoriesInSprint(Long sprintId) {
        SprintBacklog sprint = getSprintBacklogById(sprintId);
        return sprint.getUserStories();
    }

    // Pattern: Strategy pour le calcul de vélocité
    public double calculateVelocite(Long sprintId) {
        SprintBacklog sprint = getSprintBacklogById(sprintId);
        if (sprint.getUserStories().isEmpty()) {
            return 0.0;
        }

        return sprint.getUserStories().stream()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    // Pattern: Template Method pour le calcul de progression
    public double calculateProgress(Long sprintId) {
        SprintBacklog sprint = getSprintBacklogById(sprintId);
        long totalStories = sprint.getUserStories().size();

        if (totalStories == 0) return 0.0;

        long completedStories = sprint.getUserStories().stream()
                // Utilisez getStatut() au lieu de getStatus()
                .filter(us -> us.getStatut() == Status.DONE)
                .count();

        return (completedStories * 100.0) / totalStories;
    }

    public int calculateRemainingDays(Long sprintId) {
        SprintBacklog sprint = getSprintBacklogById(sprintId);

        if (sprint.getDateFin() == null || sprint.getDateDebut() == null) {
            return 0;
        }

        if (LocalDate.now().isBefore(sprint.getDateDebut())) {
            return (int) ChronoUnit.DAYS.between(LocalDate.now(), sprint.getDateFin());
        }

        return (int) ChronoUnit.DAYS.between(LocalDate.now(), sprint.getDateFin());
    }

    // Pattern: Observer pour notifier les changements de statut
    @Transactional
    public SprintBacklog updateSprintStatus(Long sprintId, Status newStatus) {
        SprintBacklog sprint = getSprintBacklogById(sprintId);

        validateStatusTransition(sprint.getStatus(), newStatus);
        sprint.setStatus(newStatus);

        // Si le sprint est terminé, marquer toutes les User Stories comme DONE
        if (newStatus == Status.DONE) {
            sprint.getUserStories().forEach(us -> us.setStatut(Status.DONE));
        }

        return sprintBacklogRepository.save(sprint);
    }

    // Méthodes privées pour la validation
    private void validateSprintDates(SprintBacklog sprint) {
        if (sprint.getDateDebut() != null && sprint.getDateFin() != null) {
            if (sprint.getDateDebut().isAfter(sprint.getDateFin())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Start date cannot be after end date"
                );
            }

            if (sprint.getDateDebut().isBefore(LocalDate.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Start date cannot be in the past"
                );
            }
        }

        if (sprint.getDureeJours() != null && sprint.getDureeJours() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duration must be greater than 0"
            );
        }
    }

    private void validateStatusTransition(Status current, Status next) {
        // Règles de transition (ex: TODO → IN_PROGRESS → DONE)
        if (current == Status.DONE && next != Status.DONE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot change status from DONE"
            );
        }
    }

    private void updateSprintFields(SprintBacklog existing, SprintBacklog updated) {
        if (updated.getNom() != null) existing.setNom(updated.getNom());
        if (updated.getDateDebut() != null) existing.setDateDebut(updated.getDateDebut());
        if (updated.getDateFin() != null) existing.setDateFin(updated.getDateFin());
        if (updated.getDureeJours() != null) existing.setDureeJours(updated.getDureeJours());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
    }
}