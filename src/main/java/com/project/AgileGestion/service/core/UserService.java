package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.User;
import com.project.AgileGestion.entity.enums.Role;
import com.project.AgileGestion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Pattern: Factory Method avec validation
    @Transactional
    public User createUser(User user) {
        validateUser(user);

        // Si c'est le premier utilisateur, le mettre comme admin
        if (userRepository.count() == 0) {
            user.setRole(Role.PRODUCT_OWNER);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(User user) {
        validateUser(user);

        // Pour l'inscription publique, limiter aux rôles basiques
        if (user.getRole() == null) {
            user.setRole(Role.DEVELOPER); // Rôle par défaut pour les inscriptions
        } else if (user.getRole() == Role.PRODUCT_OWNER || user.getRole() == Role.SCRUM_MASTER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot self-assign elevated roles during registration"
            );
        }
        user.setActive(true);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("User with id %d not found", id)
                ));
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);

        // Mise à jour des champs
        updateUserFieldsSimplified(existing, updatedUser);

        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Cannot delete: User with id %d not found", id)
            );
        }

        User user = getUserById(id);

        // Empêcher la suppression de l'admin par défaut
        if (user.getUsername().equals("admin")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot delete default admin user"
            );
        }

        // Vérifier si l'utilisateur a des tâches actives
        if (userRepository.hasActiveTasks(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete user with active tasks. Reassign tasks first."
            );
        }

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }


    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Pattern: Command pour activer/désactiver un utilisateur
    @Transactional
    public User toggleUserStatus(Long userId, boolean active) {
        User user = getUserById(userId);

        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public User changeUserRole(Long userId, Role newRole) {
        User user = getUserById(userId);

        // Sans authentification, on peut changer n'importe quel rôle
        user.setRole(newRole);
        return userRepository.save(user);
    }

    // Validation simplifiée
    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required"
            );
        }

        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username must be between 3 and 50 characters"
            );
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        // Validation d'email simple
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email format"
            );
        }

        if (user.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        // Vérifier l'unicité
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("Username '%s' already exists", user.getUsername())
            );
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("Email '%s' already exists", user.getEmail())
            );
        }
    }

    // Mise à jour simplifiée sans vérification de permissions
    private void updateUserFieldsSimplified(User existing, User updated) {
        if (updated.getUsername() != null && !updated.getUsername().equals(existing.getUsername())) {
            if (userRepository.existsByUsername(updated.getUsername())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username already exists"
                );
            }
            existing.setUsername(updated.getUsername());
        }

        if (updated.getEmail() != null && !updated.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(updated.getEmail())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already exists"
                );
            }
            existing.setEmail(updated.getEmail());
        }

        if (updated.getRole() != null) {
            existing.setRole(updated.getRole());
        }

        if (updated.getActive() != null) {
            existing.setActive(updated.getActive());
        }
    }

    // Statistiques utilisateurs
    @Transactional(readOnly = true)
    public UserStats getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByActiveTrue().size();
        long developers = userRepository.countByRole(Role.DEVELOPER);
        long scrumMasters = userRepository.countByRole(Role.SCRUM_MASTER);
        long productOwners = userRepository.countByRole(Role.PRODUCT_OWNER);

        return UserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .developers(developers)
                .scrumMasters(scrumMasters)
                .productOwners(productOwners)
                .build();
    }

    // Record pour les statistiques
    @lombok.Builder
    public record UserStats(
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            long developers,
            long scrumMasters,
            long productOwners
    ) {}
}