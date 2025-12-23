package org.example.foodanddrinkproject.repository;

import jakarta.persistence.LockModeType;
import org.example.foodanddrinkproject.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.isActive = true ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold, Pageable pageable);
}