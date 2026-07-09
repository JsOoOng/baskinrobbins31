package com.kiosk.customer.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
        SELECT p, sp.isSoldOut
        FROM Product p
        JOIN StoreProduct sp
            ON p.id = sp.product.id
        WHERE sp.store.id = :storeId
          AND p.category.id = :categoryId
          AND p.isDisplay = true
          AND sp.isDeleted = false
    """)
    List<Object[]> findProductsWithSoldOutStatus(
            @Param("storeId") Long storeId,
            @Param("categoryId") Long categoryId
    );
}