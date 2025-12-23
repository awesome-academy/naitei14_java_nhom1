package org.example.foodanddrinkproject.repository;

import org.example.foodanddrinkproject.entity.Rating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    @Query("SELECT r FROM Rating r JOIN FETCH r.product JOIN FETCH r.user ORDER BY r.createdAt DESC")
    List<Rating> findRecentRatings(Pageable pageable);
}
