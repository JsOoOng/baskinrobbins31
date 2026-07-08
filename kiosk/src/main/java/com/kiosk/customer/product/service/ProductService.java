package com.kiosk.customer.product.service;

import java.util.List;

import com.kiosk.customer.product.dto.OrderCreateRequest;
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;

public interface ProductService {
    List<ProductListResponse> getProductsByCategory(Long storeId, Long categoryId);
    ProductDetailResponse getProductDetail(Long storeId, Long productId);
    void addProduct(ProductCreateRequest request);
    
    // 이 메서드의 파라미터 타입이 아래 구현체와 완벽히 일치해야 합니다.
    void createOrder(OrderCreateRequest request);
}