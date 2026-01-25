package com.project.AgileGestion.entity;

import com.project.AgileGestion.entity.enums.Status;
import com.project.AgileGestion.entity.enums.PriorityLevel;
import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "user_stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "acceptance_criteria", columnDefinition = "TEXT")
    private String criteresAcceptation;

    @Column(name = "story_points")
    @Builder.Default
    private Integer storyPoints = 0;

    @Column(name = "priority_order")
    private Integer priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status statut = Status.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level")
    @Builder.Default
    private PriorityLevel priorityLevel = PriorityLevel.SHOULD_HAVE;

    // Relations (simples pour commencer)
    @Column(name = "epic_id")
    private Long epicId;

    // ⭐ AJOUTEZ CE GETTER si vous utilisez Lombok @Data
    @Column(name = "backlog_id", nullable = false)
    private Long backlogId;

    // AJOUTER CETTE RELATION (OBLIGATOIRE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_backlog_id")  // Crée la colonne FK dans la table user_stories
    private SprintBacklog sprintBacklog;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}