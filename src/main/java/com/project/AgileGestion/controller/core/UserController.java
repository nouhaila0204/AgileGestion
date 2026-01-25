package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.User;
import com.project.AgileGestion.entity.enums.Role;
import com.project.AgileGestion.service.core.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Créer un nouvel utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Conflit (username/email déjà existant)")
    })
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription publique d'un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscription réussie"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Rôle non autorisé pour l'inscription")
    })
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        User registered = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<User> getUserById(
            @PathVariable @Parameter(description = "ID de l'utilisateur") Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les utilisateurs")
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    @Operation(summary = "Récupérer les utilisateurs actifs")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> activeUsers = userService.getActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Récupérer les utilisateurs par rôle")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Rechercher un utilisateur par username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<User> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Rechercher un utilisateur par email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Conflit (username/email déjà existant)")
    })
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "403", description = "Suppression non autorisée"),
            @ApiResponse(responseCode = "409", description = "L'utilisateur a des tâches actives")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activer/désactiver un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<User> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        User updated = userService.toggleUserStatus(id, active);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Changer le rôle d'un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rôle mis à jour"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<User> changeUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        User updated = userService.changeUserRole(id, role);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Obtenir les statistiques des utilisateurs")
    public ResponseEntity<UserService.UserStats> getUserStatistics() {
        UserService.UserStats stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur interne du serveur"));
    }
}