package org.example.foodanddrinkproject.repository;

import org.example.foodanddrinkproject.entity.Order;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus = 'COMPLETED'")
    BigDecimal sumTotalAmount();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'COMPLETED'")
    Long countCompletedOrders();
    
    // Monthly statistics - Revenue
    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "AND YEAR(o.createdAt) = :year " +
           "AND MONTH(o.createdAt) = :month")
    BigDecimal sumTotalAmountByMonth(@Param("month") int month, @Param("year") int year);
    
    // Monthly statistics - Completed orders count
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "AND YEAR(o.createdAt) = :year " +
           "AND MONTH(o.createdAt) = :month")
    Long countCompletedOrdersByMonth(@Param("month") int month, @Param("year") int year);
    
    // Monthly statistics - Total orders count (all statuses)
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE YEAR(o.createdAt) = :year " +
           "AND MONTH(o.createdAt) = :month")
    Long countOrdersByMonth(@Param("month") int month, @Param("year") int year);
    
    // Top selling products (all time)
    @Query("SELECT oi.product.name as productName, " +
           "SUM(oi.quantity) as quantitySold, " +
           "SUM(oi.priceAtPurchase * oi.quantity) as revenue " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
    
    // Top selling products (by month)
    @Query("SELECT oi.product.name as productName, " +
           "SUM(oi.quantity) as quantitySold, " +
           "SUM(oi.priceAtPurchase * oi.quantity) as revenue " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "AND YEAR(o.createdAt) = :year " +
           "AND MONTH(o.createdAt) = :month " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsByMonth(@Param("month") int month, @Param("year") int year, Pageable pageable);
    
    // Top customers by spending (all time)
    @Query("SELECT o.user.fullName as customerName, " +
           "o.user.email as customerEmail, " +
           "COUNT(o) as orderCount, " +
           "SUM(o.totalAmount) as totalSpent " +
           "FROM Order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "GROUP BY o.user.id, o.user.fullName, o.user.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopCustomers(Pageable pageable);
    
    // Top customers by spending (by month)
    @Query("SELECT o.user.fullName as customerName, " +
           "o.user.email as customerEmail, " +
           "COUNT(o) as orderCount, " +
           "SUM(o.totalAmount) as totalSpent " +
           "FROM Order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "AND YEAR(o.createdAt) = :year " +
           "AND MONTH(o.createdAt) = :month " +
           "GROUP BY o.user.id, o.user.fullName, o.user.email " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopCustomersByMonth(@Param("month") int month, @Param("year") int year, Pageable pageable);

}
