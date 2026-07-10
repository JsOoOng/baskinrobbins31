package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Product;
import com.kiosk.entity.ProductOption;
import com.kiosk.headquarter.dto.product.HeadProductOptionCreateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionResponseDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadProductMapper;
import com.kiosk.headquarter.repository.HeadProductOptionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadProductOptionService {

    private final HeadProductOptionMapper headProductOptionMapper;
    private final HeadProductMapper headProductMapper;

    // 상품 옵션 등록
    @Transactional
    public String createProductOption(
            Integer productId,
            HeadProductOptionCreateRequestDTO requestDTO) {

        Product product = headProductMapper.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        boolean alreadyExists = headProductOptionMapper
                .existsByProduct_IdAndOptionTypeAndOptionName(
                        productId,
                        requestDTO.getOptionType(),
                        requestDTO.getOptionName()
                );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 등록된 상품 옵션입니다.");
        }

        ProductOption productOption = ProductOption.builder()
                .product(product)
                .optionType(requestDTO.getOptionType())
                .optionName(requestDTO.getOptionName())
                .extraPrice(
                        requestDTO.getExtraPrice() != null
                                ? requestDTO.getExtraPrice()
                                : 0
                )
                .maxFlavorCount(
                        requestDTO.getMaxFlavorCount() != null
                                ? requestDTO.getMaxFlavorCount()
                                : 0
                )
                .build();

        headProductOptionMapper.save(productOption);

        return "상품 옵션 등록 성공";
    }

    // 상품 옵션 목록 조회
    public List<HeadProductOptionResponseDTO> getProductOptionList(Integer productId) {

        return headProductOptionMapper.findByProduct_IdOrderByIdDesc(productId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 상품 옵션 상세 조회
    public HeadProductOptionResponseDTO getProductOptionDetail(
            Integer productId,
            Integer optionId) {

        ProductOption productOption = headProductOptionMapper
                .findByProduct_IdAndId(productId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 옵션입니다."));

        return toResponseDTO(productOption);
    }

    // 상품 옵션 수정
    @Transactional
    public String updateProductOption(
            Integer productId,
            Integer optionId,
            HeadProductOptionUpdateRequestDTO requestDTO) {

        ProductOption productOption = headProductOptionMapper
                .findByProduct_IdAndId(productId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 옵션입니다."));

        productOption.updateOption(
                requestDTO.getOptionType(),
                requestDTO.getOptionName(),
                requestDTO.getExtraPrice() != null ? requestDTO.getExtraPrice() : 0,
                requestDTO.getMaxFlavorCount() != null ? requestDTO.getMaxFlavorCount() : 0
        );

        return "상품 옵션 수정 성공";
    }

    // 상품 옵션 삭제
    @Transactional
    public String deleteProductOption(Integer productId, Integer optionId) {

        ProductOption productOption = headProductOptionMapper
                .findByProduct_IdAndId(productId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 옵션입니다."));

        headProductOptionMapper.delete(productOption);

        return "상품 옵션 삭제 성공";
    }

    private HeadProductOptionResponseDTO toResponseDTO(ProductOption productOption) {
        return HeadProductOptionResponseDTO.builder()
                .optionId(productOption.getId())
                .productId(productOption.getProduct().getId())
                .productName(productOption.getProduct().getProductName())
                .optionType(productOption.getOptionType())
                .optionName(productOption.getOptionName())
                .extraPrice(productOption.getExtraPrice())
                .maxFlavorCount(productOption.getMaxFlavorCount())
                .build();
    }
}