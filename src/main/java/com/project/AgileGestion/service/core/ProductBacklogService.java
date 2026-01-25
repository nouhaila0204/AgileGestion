package com.project.AgileGestion.service.core;

import com.project.AgileGestion.entity.ProductBacklog;
import com.project.AgileGestion.repository.ProductBacklogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service CORE pour ProductBacklog
 * Responsabilité : CRUD uniquement (comme UserStoryService et EpicService)
 */
@Service
@Transactional
public class ProductBacklogService {

    private final ProductBacklogRepository productBacklogRepository;

    public ProductBacklogService(ProductBacklogRepository productBacklogRepository) {
        this.productBacklogRepository = productBacklogRepository;
    }

    // ========== CRUD BASIQUE ==========

    public ProductBacklog createProductBacklog(ProductBacklog productBacklog) {
        validateProductBacklog(productBacklog);
        return productBacklogRepository.save(productBacklog);
    }

    public List<ProductBacklog> getAllProductBacklogs() {
        return productBacklogRepository.findAll();
    }

    public ProductBacklog getProductBacklogById(Long id) {
        return productBacklogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductBacklog non trouvé avec id: " + id));
    }

    public ProductBacklog updateProductBacklog(Long id, ProductBacklog productBacklogDetails) {
        ProductBacklog existing = getProductBacklogById(id);

        if (productBacklogDetails.getNom() != null) {
            existing.setNom(productBacklogDetails.getNom());
        }
        if (productBacklogDetails.getDescription() != null) {
            existing.setDescription(productBacklogDetails.getDescription());
        }
        if (productBacklogDetails.getProjectId() != null) {
            existing.setProjectId(productBacklogDetails.getProjectId());
        }

        return productBacklogRepository.save(existing);
    }

    public void deleteProductBacklog(Long id) {
        ProductBacklog backlog = getProductBacklogById(id);
        productBacklogRepository.delete(backlog);
    }

    // ========== REQUÊTES SIMPLES ==========

    public List<ProductBacklog> getByProjectId(Long projectId) {
        return productBacklogRepository.findByProjectId(projectId);
    }

    // ========== MÉTHODES PRIVÉES ==========

    private void validateProductBacklog(ProductBacklog productBacklog) {
        if (productBacklog.getNom() == null || productBacklog.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du ProductBacklog est obligatoire");
        }
    }
}