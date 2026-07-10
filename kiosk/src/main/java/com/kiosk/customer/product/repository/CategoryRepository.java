package com.kiosk.customer.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}