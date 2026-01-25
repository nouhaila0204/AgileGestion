package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.SprintBacklog;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import com.project.AgileGestion.repository.SprintBacklogRepository;
import com.project.AgileGestion.repository.UserStoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service CORE pour la gestion des User Stories
 * Responsabilité : CRUD + Opérations de base uniquement
 */
@Service
@Transactional
public class UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private SprintBacklogRepository sprintBacklogRepository;

    public UserStoryService(UserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }

    // ========== CRUD BASIQUE ==========

    public UserStory createUserStory(UserStory userStory) {
        validateUserStoryForCreation(userStory);
        setDefaultValues(userStory);
        return userStoryRepository.save(userStory);
    }

    public List<UserStory> getAllUserStories() {
        return userStoryRepository.findAll();
    }

    public UserStory getUserStoryById(Long id) {
        return userStoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserStory non trouvée avec id: " + id));
    }

    public UserStory updateUserStory(Long id, UserStory userStoryDetails) {
        UserStory existingStory = getUserStoryById(id);
        validateUserStoryForUpdate(userStoryDetails);
        updateStoryFields(existingStory, userStoryDetails);
        return userStoryRepository.save(existingStory);
    }

    public void deleteUserStory(Long id) {
        UserStory story = getUserStoryById(id);
        userStoryRepository.delete(story);
    }

    // ========== GESTION DES STATUTS ==========

    public UserStory updateStatus(Long id, Status newStatus) {
        UserStory story = getUserStoryById(id);
        story.setStatut(newStatus);
        return userStoryRepository.save(story);
    }

    public List<UserStory> getByStatus(Status status) {
        return userStoryRepository.findByStatut(status);
    }

    // ========== GESTION DES PRIORITÉS ==========

    public UserStory updatePriority(Long id, Integer newPriority) {
        if (newPriority < 1 || newPriority > 10) {
            throw new IllegalArgumentException("La priorité doit être comprise entre 1 et 10");
        }
        UserStory story = getUserStoryById(id);
        story.setPriorite(newPriority);
        return userStoryRepository.save(story);
    }

    public UserStory updatePriorityLevel(Long id, PriorityLevel priorityLevel) {
        UserStory story = getUserStoryById(id);
        story.setPriorityLevel(priorityLevel);
        return userStoryRepository.save(story);
    }

    public List<UserStory> getSortedByPriority(Long backlogId) {
        return userStoryRepository.findByBacklogIdOrderByPrioriteAsc(backlogId);
    }

    // ========== REQUÊTES SIMPLES ==========

    public List<UserStory> getByProductBacklog(Long backlogId) {
        return userStoryRepository.findByBacklogId(backlogId);
    }

    public List<UserStory> getByEpic(Long epicId) {
        return userStoryRepository.findByEpicId(epicId);
    }

    public UserStory assignToEpic(Long storyId, Long epicId) {
        UserStory story = getUserStoryById(storyId);
        story.setEpicId(epicId);
        return userStoryRepository.save(story);
    }

    public UserStory assignToProductBacklog(Long storyId, Long backlogId) {
        UserStory story = getUserStoryById(storyId);
        story.setBacklogId(backlogId);
        return userStoryRepository.save(story);
    }

    public UserStory assignToSprintBacklog(Long storyId, Long sprintBacklogId) {
        UserStory story = getUserStoryById(storyId);

        // ⭐ Vous devez récupérer le SprintBacklog via son repository
        // Assurez-vous d'avoir un SprintBacklogRepository
        SprintBacklog sprintBacklog = sprintBacklogRepository.findById(sprintBacklogId)
                .orElseThrow(() -> new RuntimeException("SprintBacklog non trouvé avec id: " + sprintBacklogId));

        story.setSprintBacklog(sprintBacklog);
        return userStoryRepository.save(story);
    }

    // ========== MÉTHODES PRIVÉES (VALIDATION) ==========

    private void validateUserStoryForCreation(UserStory userStory) {
        if (userStory.getTitre() == null || userStory.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (userStory.getBacklogId() == null) {
            throw new IllegalArgumentException("Le Product Backlog ID est obligatoire");
        }
        validateStoryPoints(userStory.getStoryPoints());
    }

    private void validateUserStoryForUpdate(UserStory userStory) {
        if (userStory.getTitre() != null && userStory.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }
        validateStoryPoints(userStory.getStoryPoints());
    }

    private void validateStoryPoints(Integer storyPoints) {
        if (storyPoints != null) {
            int[] FIBONACCI_POINTS = {0, 1, 2, 3, 5, 8, 13, 20, 40, 100};
            boolean isValid = false;
            for (int validPoint : FIBONACCI_POINTS) {
                if (validPoint == storyPoints) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                throw new IllegalArgumentException(
                        "Les story points doivent suivre la suite de Fibonacci: 0,1,2,3,5,8,13,20,40,100"
                );
            }
        }
    }

    private void setDefaultValues(UserStory userStory) {
        if (userStory.getStatut() == null) {
            userStory.setStatut(Status.TODO);
        }
        if (userStory.getPriorityLevel() == null) {
            userStory.setPriorityLevel(PriorityLevel.SHOULD_HAVE);
        }
        if (userStory.getStoryPoints() == null) {
            userStory.setStoryPoints(0);
        }
        if (userStory.getPriorite() == null) {
            userStory.setPriorite(5);
        }
    }

    private void updateStoryFields(UserStory existing, UserStory updates) {
        if (updates.getTitre() != null) existing.setTitre(updates.getTitre());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getCriteresAcceptation() != null) existing.setCriteresAcceptation(updates.getCriteresAcceptation());
        if (updates.getStoryPoints() != null) existing.setStoryPoints(updates.getStoryPoints());
        if (updates.getPriorite() != null) existing.setPriorite(updates.getPriorite());
        if (updates.getStatut() != null) existing.setStatut(updates.getStatut());
        if (updates.getPriorityLevel() != null) existing.setPriorityLevel(updates.getPriorityLevel());
        if (updates.getEpicId() != null) existing.setEpicId(updates.getEpicId());
    }
}