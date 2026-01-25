package com.project.AgileGestion.entity;

import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;

    private Integer estimationHeures;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;  // ⭐ ID seulement, pas d'entité

    @Column(name = "user_story_id", nullable = false)
    private Long userStoryId;  // ⭐ ID seulement, pas d'entité

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_backlog_id")
    private SprintBacklog sprintBacklog;

    private LocalDate dateCreation = LocalDate.now();
    private LocalDate dateCompletion;
    // ⭐ Méthode pour récupérer l'ID de SprintBacklog facilement
    public Long getSprintBacklogId() {
        return this.sprintBacklog != null ? this.sprintBacklog.getId() : null;
    }


    public void setUserStory(UserStory story) {
    }
}