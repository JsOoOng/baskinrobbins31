package com.kiosk.customer.category.service;

import java.util.List;

import com.kiosk.customer.flavor.category.dto.CategoryResponse;

public interface CategoryService {

	List<CategoryResponse> getAllCategories();
}
