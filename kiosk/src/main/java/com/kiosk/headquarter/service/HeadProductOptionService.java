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

/**
 * [코드 흐름 안내] HeadProductOptionService
 *
 * <p>역할: 본사 관리의 상품 옵션 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadProductOptionMapper, HeadProductMapper -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadProductOptionService {

    private final HeadProductOptionMapper headProductOptionMapper;
    private final HeadProductMapper headProductMapper;
    private final AdminLogService adminLogService;

    // 상품 옵션 등록
    @Transactional
    /**
     * [메서드 흐름] createProductOption
     * Controller 또는 상위 서비스에서 호출되어 HeadProductOptionMapper, HeadProductMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

        adminLogService.logAction("상품 옵션",
                product.getProductName() + " - " + productOption.getOptionName() + " 옵션 등록");
        return "상품 옵션 등록 성공";
    }

    // 상품 옵션 목록 조회
    /**
     * [메서드 흐름] getProductOptionList
     * Controller 또는 상위 서비스에서 호출되어 HeadProductOptionMapper, HeadProductMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadProductOptionResponseDTO> getProductOptionList(Integer productId) {

        return headProductOptionMapper.findByProduct_IdOrderByIdDesc(productId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 상품 옵션 상세 조회
    /**
     * [메서드 흐름] getProductOptionDetail
     * Controller 또는 상위 서비스에서 호출되어 HeadProductOptionMapper, HeadProductMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] updateProductOption
     * Controller 또는 상위 서비스에서 호출되어 HeadProductOptionMapper, HeadProductMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

        adminLogService.logAction("상품 옵션",
                productOption.getProduct().getProductName() + " - " + productOption.getOptionName() + " 옵션 수정");
        return "상품 옵션 수정 성공";
    }

    // 상품 옵션 삭제
    @Transactional
    /**
     * [메서드 흐름] deleteProductOption
     * Controller 또는 상위 서비스에서 호출되어 HeadProductOptionMapper, HeadProductMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String deleteProductOption(Integer productId, Integer optionId) {

        ProductOption productOption = headProductOptionMapper
                .findByProduct_IdAndId(productId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 옵션입니다."));

        String action = productOption.getProduct().getProductName()
                + " - " + productOption.getOptionName() + " 옵션 삭제";
        headProductOptionMapper.delete(productOption);

        adminLogService.logAction("상품 옵션", action);
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
