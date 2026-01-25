package com.project.AgileGestion.repository;

import com.project.AgileGestion.entity.User;
import com.project.AgileGestion.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Recherche d'utilisateurs actifs
    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    List<User> findByRoleAndActiveTrue(Role role);

    // Recherche par nom d'utilisateur contenant une chaîne
    List<User> findByUsernameContainingIgnoreCase(String username);

    // Recherche par email contenant une chaîne
    List<User> findByEmailContainingIgnoreCase(String email);

    // Compter les utilisateurs par rôle
    long countByRole(Role role);

    // Trouver les utilisateurs assignés à des tâches dans un sprint
    @Query("SELECT DISTINCT u FROM User u JOIN Task t ON u.id = t.assignedToUserId " +
            "WHERE t.sprintBacklog.id = :sprintId")
    List<User> findUsersAssignedToSprint(@Param("sprintId") Long sprintId);

    // Trouver les utilisateurs avec un certain nombre de tâches assignées
    @Query("SELECT u, COUNT(t) as taskCount FROM User u LEFT JOIN Task t ON u.id = t.assignedToUserId " +
            "GROUP BY u.id HAVING COUNT(t) >= :minTasks")
    List<Object[]> findUsersWithMinTasks(@Param("minTasks") long minTasks);

    // Vérifier si un utilisateur a des tâches en cours
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Task t WHERE t.assignedToUserId = :userId AND t.status != 'DONE'")
    boolean hasActiveTasks(@Param("userId") Long userId);
}