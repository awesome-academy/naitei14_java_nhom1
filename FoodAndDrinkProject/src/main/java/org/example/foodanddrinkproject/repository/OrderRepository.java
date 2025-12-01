package org.example.foodanddrinkproject.repository;

import org.example.foodanddrinkproject.entity.Order;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

}
