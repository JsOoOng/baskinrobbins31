package com.kiosk.headquarter.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Category;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Product;
import com.kiosk.headquarter.dto.product.HeadProductCreateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductResponseDTO;
import com.kiosk.headquarter.repository.HeadCategoryMapper;
import com.kiosk.headquarter.repository.HeadInventoryItemMapper;
import com.kiosk.headquarter.repository.HeadProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadProductService {

	private final HeadProductMapper headProductMapper;
	private final HeadCategoryMapper headCategoryMapper;
	private final HeadInventoryItemMapper headInventoryItemMapper;

	// 본사 상품 등록
	@Transactional
	public HeadProductResponseDTO createProduct(HeadProductCreateRequestDTO requestDTO) {
		
		
		Category category = headCategoryMapper.findById(requestDTO.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

		
		Product product = Product.builder().category(category).productName(requestDTO.getProductName())
				.description(requestDTO.getDescription()).basePrice(requestDTO.getBasePrice())
				.discountRate(requestDTO.getDiscountRate() != null ? requestDTO.getDiscountRate() : BigDecimal.ZERO)
				.isDisplay(requestDTO.getIsDisplay() != null ? requestDTO.getIsDisplay() : true)
				.imageUrl(requestDTO.getImageUrl()).build();

		Product savedProduct = headProductMapper.save(product);

		System.out.println("상품 저장 완료 : " + savedProduct.getId());

		InventoryItem inventoryItem = InventoryItem.builder().product(savedProduct).unit("EA")
				.unitPrice(savedProduct.getBasePrice()).build();

		System.out.println("재고 저장 직전");

		InventoryItem savedItem = headInventoryItemMapper.saveAndFlush(inventoryItem);

		System.out.println("재고 저장 완료 : " + savedItem.getId());

		return toResponseDTO(savedProduct);
	}

	// 본사 상품 목록 조회
	public List<HeadProductResponseDTO> getProductList() {

		return headProductMapper.findByIsDisplayTrueOrderByIdDesc().stream().map(this::toResponseDTO).toList();
	}

	// 본사 상품 상세 조회
	public HeadProductResponseDTO getProductDetail(Integer productId) {

		Product product = headProductMapper.findByIdAndIsDisplayTrue(productId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

		return toResponseDTO(product);
	}

	// 본사 상품 수정
	@Transactional
	public HeadProductResponseDTO updateProduct(Integer productId, HeadProductCreateRequestDTO requestDTO) {

		Product product = headProductMapper.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

		Category category = headCategoryMapper.findById(requestDTO.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

		product.updateProduct(category, requestDTO.getProductName(), requestDTO.getDescription(),
				requestDTO.getBasePrice(),
				requestDTO.getDiscountRate() != null ? requestDTO.getDiscountRate() : BigDecimal.ZERO,
				requestDTO.getIsDisplay() != null ? requestDTO.getIsDisplay() : true, product.getImageUrl() // 🌟 기존 이미지
																											// URL 유지
		);

		return toResponseDTO(product);
	}

	// 본사 상품 삭제 처리
	@Transactional
	public void deleteProduct(Integer productId) {

		Product product = headProductMapper.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

		product.hideProduct();
	}

	private HeadProductResponseDTO toResponseDTO(Product product) {

		return HeadProductResponseDTO.builder().productId(product.getId()).categoryId(product.getCategory().getId())
				.categoryName(product.getCategory().getCategoryName()).productName(product.getProductName())
				.description(product.getDescription()).basePrice(product.getBasePrice())
				.discountRate(product.getDiscountRate()).isDisplay(product.getIsDisplay())
				.imageUrl(product.getImageUrl()).createdAt(product.getCreatedAt()).build();
	}
}