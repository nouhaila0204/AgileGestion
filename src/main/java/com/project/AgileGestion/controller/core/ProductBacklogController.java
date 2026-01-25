package com.project.AgileGestion.controller.core;

import com.project.AgileGestion.entity.ProductBacklog;
import com.project.AgileGestion.service.core.ProductBacklogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-backlogs")
public class ProductBacklogController {

    private final ProductBacklogService productBacklogService;

    @Autowired
    public ProductBacklogController(ProductBacklogService productBacklogService) {
        this.productBacklogService = productBacklogService;
    }

    @PostMapping
    public ResponseEntity<?> createProductBacklog(@RequestBody ProductBacklog productBacklog) {
        try {
            ProductBacklog created = productBacklogService.createProductBacklog(productBacklog);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Données du backlog invalides", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du backlog", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProductBacklogs() {
        try {
            List<ProductBacklog> backlogs = productBacklogService.getAllProductBacklogs();
            return ResponseEntity.ok(backlogs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des backlogs", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductBacklogById(@PathVariable Long id) {
        try {
            ProductBacklog backlog = productBacklogService.getProductBacklogById(id);
            return ResponseEntity.ok(backlog);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération du backlog", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProductBacklog(
            @PathVariable Long id,
            @RequestBody ProductBacklog productBacklogDetails) {
        try {
            ProductBacklog updated = productBacklogService.updateProductBacklog(id, productBacklogDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour du backlog", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductBacklog(@PathVariable Long id) {
        try {
            productBacklogService.deleteProductBacklog(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backlog non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression du backlog", "message", e.getMessage()));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getByProjectId(@PathVariable Long projectId) {
        try {
            List<ProductBacklog> backlogs = productBacklogService.getByProjectId(projectId);
            return ResponseEntity.ok(backlogs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Projet non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des backlogs", "message", e.getMessage()));
        }
    }
}