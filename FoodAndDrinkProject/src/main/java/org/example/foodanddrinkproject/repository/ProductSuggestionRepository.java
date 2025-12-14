package org.example.foodanddrinkproject.repository;

import org.example.foodanddrinkproject.entity.ProductSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSuggestionRepository extends JpaRepository<ProductSuggestion, Long> {
    Page<ProductSuggestion> findByProductId(Long productId, Pageable pageable);

    Page<ProductSuggestion> findAllByOrderByCreatedAtDesc(Pageable pageable);
}