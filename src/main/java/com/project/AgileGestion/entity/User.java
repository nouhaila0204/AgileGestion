package com.project.AgileGestion.entity;

import com.project.AgileGestion.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    private Boolean active = true;

    // Méthodes utilitaires (optionnelles)
    public boolean isProductOwner() {
        return this.role == Role.PRODUCT_OWNER;
    }

    public boolean isScrumMaster() {
        return this.role == Role.SCRUM_MASTER;
    }

    public boolean isDeveloper() {
        return this.role == Role.DEVELOPER;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }
}