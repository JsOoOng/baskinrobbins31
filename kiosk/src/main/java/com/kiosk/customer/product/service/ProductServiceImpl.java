package com.kiosk.customer.product.service;

import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;
import com.kiosk.customer.product.dto.OrderCreateRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Override
    public List<ProductListResponse> getProductsByCategory(Long storeId, Long categoryId) {
        return null;
    }

    @Override
    public ProductDetailResponse getProductDetail(Long storeId, Long productId) {
        return null;
    }

    @Override
    @Transactional
    public void addProduct(ProductCreateRequest request) {
    }

    // 부모 인터페이스와 주소, 이름, 파라미터가 완벽히 일치하여 정상 오버라이딩 됩니다.
    @Override
    @Transactional
    public void createOrder(OrderCreateRequest request) {
        // [비즈니스 로직 주석 생략]
    }
}