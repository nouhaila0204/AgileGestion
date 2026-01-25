package com.project.AgileGestion.entity;

import com.project.AgileGestion.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sprint_backlogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer dureeJours;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;

    // ⭐ CORRECTION 3 : Relation avec UserStory (déplacées depuis ProductBacklog)
    @OneToMany(mappedBy = "sprintBacklog", fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserStory> userStories = new ArrayList<>();

    // ⭐ CORRECTION 4 : Relation avec Task (selon 6.1 et 6.2)
    @OneToMany(mappedBy = "sprintBacklog", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();



}