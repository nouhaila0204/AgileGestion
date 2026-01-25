package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.Epic;
import com.project.AgileGestion.entity.UserStory;
import com.project.AgileGestion.repository.EpicRepository;
import com.project.AgileGestion.repository.ProductBacklogRepository;
import com.project.AgileGestion.repository.UserStoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class EpicService {

    private final EpicRepository epicRepository;

    private final ProductBacklogRepository productBacklogRepository;

    private final UserStoryRepository userStoryRepository;

    public EpicService(EpicRepository epicRepository, ProductBacklogRepository productBacklogRepository, UserStoryRepository userStoryRepository) {
        this.epicRepository = epicRepository;
        this.productBacklogRepository = productBacklogRepository;
        this.userStoryRepository = userStoryRepository;
    }

    // ========== CRUD BASIQUE (Pour Product Owner) ==========

    public Epic createEpic(Epic epic) {
        validateEpic(epic);

        boolean backlogExists = productBacklogRepository.existsById(epic.getBacklogId());
        if (!backlogExists) {
            throw new RuntimeException("ProductBacklog non trouvé avec id: " + epic.getBacklogId());
        }

        boolean titleExists = epicRepository.existsByBacklogIdAndTitre(
                epic.getBacklogId(), epic.getTitre());
        if (titleExists) {
            throw new IllegalArgumentException("Un Epic avec ce titre existe déjà dans ce backlog");
        }

        return epicRepository.save(epic);
    }

    public List<Epic> getAllEpics() {
        return epicRepository.findAll();
    }

    public Epic getEpicById(Long id) {
        Optional<Epic> epic = epicRepository.findById(id);
        if (!epic.isPresent()) {
            throw new RuntimeException("Epic non trouvé avec id: " + id);
        }
        return epic.get();
    }

    public Epic getEpicWithUserStories(Long id) {
        Optional<Epic> epic = epicRepository.findByIdWithUserStories(id);
        if (!epic.isPresent()) {
            throw new RuntimeException("Epic non trouvé avec id: " + id);
        }
        return epic.get();
    }

    public Epic updateEpic(Long id, Epic epicDetails) {
        Epic existing = getEpicById(id);

        if (epicDetails.getTitre() != null) {
            if (!existing.getTitre().equals(epicDetails.getTitre())) {
                boolean titleExists = epicRepository.existsByBacklogIdAndTitre(
                        existing.getBacklogId(), epicDetails.getTitre());
                if (titleExists) {
                    throw new IllegalArgumentException("Un Epic avec ce titre existe déjà dans ce backlog");
                }
            }
            existing.setTitre(epicDetails.getTitre());
        }

        if (epicDetails.getDescription() != null) {
            existing.setDescription(epicDetails.getDescription());
        }

        if (epicDetails.getBacklogId() != null) {
            boolean backlogExists = productBacklogRepository.existsById(epicDetails.getBacklogId());
            if (!backlogExists) {
                throw new RuntimeException("ProductBacklog non trouvé avec id: " + epicDetails.getBacklogId());
            }
            existing.setBacklogId(epicDetails.getBacklogId());
        }

        return epicRepository.save(existing);
    }

    public void deleteEpic(Long id) {
        Epic epic = getEpicWithUserStories(id);
        if (epic.getUserStories() != null && !epic.getUserStories().isEmpty()) {
            for (UserStory story : epic.getUserStories()) {
                story.setEpicId(null);
                userStoryRepository.save(story);
            }
        }
        epicRepository.delete(epic);
    }

    // ========== REQUÊTES SPÉCIFIQUES ==========

    public List<Epic> getEpicsByBacklogId(Long backlogId) {
        return epicRepository.findByBacklogId(backlogId);
    }

    public List<Epic> searchEpicsByTitle(Long backlogId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getEpicsByBacklogId(backlogId);
        }
        return epicRepository.findByBacklogIdAndTitreContainingIgnoreCase(backlogId, keyword);
    }

    public Long countEpicsByBacklogId(Long backlogId) {
        return epicRepository.countByBacklogId(backlogId);
    }

    public boolean isEpicInBacklog(Long epicId, Long backlogId) {
        return epicRepository.existsByIdAndBacklogId(epicId, backlogId);
    }

    // ========== GESTION USERSTORIES ==========

    public void associateUserStoryToEpic(Long epicId, Long userStoryId) {
        Epic epic = getEpicById(epicId);
        Optional<UserStory> userStoryOpt = userStoryRepository.findById(userStoryId);

        if (!userStoryOpt.isPresent()) {
            throw new RuntimeException("UserStory non trouvée avec id: " + userStoryId);
        }

        UserStory userStory = userStoryOpt.get();

        if (!userStory.getBacklogId().equals(epic.getBacklogId())) {
            throw new IllegalArgumentException("La UserStory n'appartient pas au même ProductBacklog que l'Epic");
        }

        userStory.setEpicId(epicId);
        userStoryRepository.save(userStory);
    }

    public void detachUserStoryFromEpic(Long userStoryId) {
        Optional<UserStory> userStoryOpt = userStoryRepository.findById(userStoryId);

        if (!userStoryOpt.isPresent()) {
            throw new RuntimeException("UserStory non trouvée avec id: " + userStoryId);
        }

        UserStory userStory = userStoryOpt.get();
        userStory.setEpicId(null);
        userStoryRepository.save(userStory);
    }

    public void associateMultipleUserStoriesToEpic(Long epicId, List<Long> userStoryIds) {
        Epic epic = getEpicWithUserStories(epicId);

        for (Long userStoryId : userStoryIds) {
            Optional<UserStory> userStoryOpt = userStoryRepository.findById(userStoryId);

            if (userStoryOpt.isPresent()) {
                UserStory userStory = userStoryOpt.get();

                if (userStory.getBacklogId().equals(epic.getBacklogId())) {
                    userStory.setEpicId(epicId);
                    userStoryRepository.save(userStory);
                } else {
                    throw new IllegalArgumentException("La UserStory " + userStoryId + " n'appartient pas au même ProductBacklog que l'Epic");
                }
            } else {
                throw new RuntimeException("UserStory non trouvée avec id: " + userStoryId);
            }
        }
    }

    public List<UserStory> getUserStoriesForEpic(Long epicId) {
        Epic epic = getEpicWithUserStories(epicId);
        return epic.getUserStories();
    }

    public void transferUserStories(Long fromEpicId, Long toEpicId, List<Long> userStoryIds) {
        Epic toEpic = getEpicById(toEpicId);

        for (Long userStoryId : userStoryIds) {
            Optional<UserStory> userStoryOpt = userStoryRepository.findById(userStoryId);

            if (userStoryOpt.isPresent()) {
                UserStory userStory = userStoryOpt.get();

                if (userStory.getBacklogId().equals(toEpic.getBacklogId())) {
                    userStory.setEpicId(toEpicId);
                    userStoryRepository.save(userStory);
                } else {
                    throw new IllegalArgumentException("La UserStory " + userStoryId + " n'appartient pas au même ProductBacklog que l'Epic de destination");
                }
            } else {
                throw new RuntimeException("UserStory non trouvée avec id: " + userStoryId);
            }
        }
    }

    // ========== MÉTHODES DE CALCUL (Pour Scrum Master) ==========

    public Integer calculateTotalStoryPoints(Long epicId) {
        Epic epic = getEpicWithUserStories(epicId);
        List<UserStory> stories = epic.getUserStories();

        if (stories == null || stories.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (UserStory story : stories) {
            if (story.getStoryPoints() != null) {
                total += story.getStoryPoints();
            }
        }

        return total;
    }

    public Integer countUserStoriesInEpic(Long epicId) {
        Epic epic = getEpicWithUserStories(epicId);
        List<UserStory> stories = epic.getUserStories();

        if (stories == null) {
            return 0;
        }

        return stories.size();
    }

    public Map<String, Integer> countUserStoriesByMoSCoW(Long epicId) {
        Epic epic = getEpicWithUserStories(epicId);
        List<UserStory> stories = epic.getUserStories();
        Map<String, Integer> counts = new HashMap<>();

        if (stories == null || stories.isEmpty()) {
            return counts;
        }

        counts.put("MUST_HAVE", 0);
        counts.put("SHOULD_HAVE", 0);
        counts.put("COULD_HAVE", 0);
        counts.put("WONT_HAVE", 0);

        for (UserStory story : stories) {
            String priority = story.getPriorityLevel().name();

            if (counts.containsKey(priority)) {
                int currentCount = counts.get(priority);
                counts.put(priority, currentCount + 1);
            } else {
                counts.put(priority, 1);
            }
        }

        return counts;
    }

    // ========== STATISTIQUES (Pour Scrum Master) ==========

    public Map<String, Object> getEpicStatistics(Long epicId) {
        Epic epic = getEpicWithUserStories(epicId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("id", epic.getId());
        stats.put("titre", epic.getTitre());
        stats.put("description", epic.getDescription());
        stats.put("productBacklogId", epic.getBacklogId());
        stats.put("createdAt", epic.getCreatedAt());
        stats.put("updatedAt", epic.getUpdatedAt());

        stats.put("nombreUserStories", countUserStoriesInEpic(epicId));
        stats.put("totalStoryPoints", calculateTotalStoryPoints(epicId));
        stats.put("distributionPriorites", countUserStoriesByMoSCoW(epicId));

        return stats;
    }

    public Map<String, Object> getBacklogEpicsStatistics(Long backlogId) {
        List<Epic> epics = getEpicsByBacklogId(backlogId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("backlogId", backlogId);
        stats.put("totalEpics", epics.size());

        int totalUserStories = 0;
        int totalStoryPoints = 0;
        int epicsWithoutStories = 0;
        int epicsWithManyStories = 0;

        for (Epic epic : epics) {
            int storyCount = countUserStoriesInEpic(epic.getId());
            totalUserStories += storyCount;
            totalStoryPoints += calculateTotalStoryPoints(epic.getId());

            if (storyCount == 0) {
                epicsWithoutStories++;
            }
            if (storyCount > 5) {
                epicsWithManyStories++;
            }
        }

        stats.put("totalUserStories", totalUserStories);
        stats.put("totalStoryPoints", totalStoryPoints);
        stats.put("epicsSansUserStories", epicsWithoutStories);
        stats.put("epicsAvecPlusDe5Stories", epicsWithManyStories);

        List<Epic> epicsSansStories = epicRepository.findEpicsWithoutStories(backlogId);
        stats.put("listeEpicsSansStories", epicsSansStories.size());

        List<Epic> epicsAvecBeaucoupStories = epicRepository.findEpicsWithMoreThanXStories(backlogId, 5);
        stats.put("listeEpicsAvecBeaucoupStories", epicsAvecBeaucoupStories.size());

        Map<String, Integer> sizeDistribution = new HashMap<>();
        sizeDistribution.put("0 stories", epicsWithoutStories);
        sizeDistribution.put("1-3 stories", 0);
        sizeDistribution.put("4-5 stories", 0);
        sizeDistribution.put("6+ stories", epicsWithManyStories);

        for (Epic epic : epics) {
            int storyCount = countUserStoriesInEpic(epic.getId());
            if (storyCount >= 1 && storyCount <= 3) {
                sizeDistribution.put("1-3 stories", sizeDistribution.get("1-3 stories") + 1);
            } else if (storyCount == 4 || storyCount == 5) {
                sizeDistribution.put("4-5 stories", sizeDistribution.get("4-5 stories") + 1);
            }
        }
        stats.put("sizeDistribution", sizeDistribution);
        return stats;
    }

    public boolean canDeleteEpic(Long epicId) {
        Epic epic = getEpicWithUserStories(epicId);

        if (epic.getUserStories() != null && !epic.getUserStories().isEmpty()) {
            for (UserStory story : epic.getUserStories()) {
                if (story.getStatut() != null && story.getStatut().name().equals("IN_PROGRESS")) {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Obtenir les Epics avec leurs User Stories (4.3)
     */
    public List<Map<String, Object>> getEpicsWithStories(Long backlogId) {
        List<Epic> epics = getEpicsByBacklogId(backlogId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Epic epic : epics) {
            Map<String, Object> epicData = new HashMap<>();
            epicData.put("epic", epic);

            List<UserStory> epicStories = getUserStoriesForEpic(epic.getId());
            epicData.put("storis", epicStories);
            epicData.put("storyCount", epicStories.size());

            // Calculer les points totaux pour cet Epic
            int epicPoints = epicStories.stream()
                    .mapToInt(story -> story.getStoryPoints() != null ? story.getStoryPoints() : 0)
                    .sum();
            epicData.put("totalPoints", epicPoints);

            result.add(epicData);
        }

        return result;
    }

    // ========== MÉTHODES PRIVÉES ==========

    private void validateEpic(Epic epic) {
        if (epic.getTitre() == null || epic.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'Epic est obligatoire");
        }
        if (epic.getBacklogId() == null) {
            throw new IllegalArgumentException("L'Epic doit être associé à un ProductBacklog");
        }
        if (epic.getTitre().length() > 200) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser 200 caractères");
        }
    }
}