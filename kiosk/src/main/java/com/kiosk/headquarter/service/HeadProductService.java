package com.kiosk.headquarter.service;

import com.kiosk.headquarter.dto.product.HeadProductCreateRequest;
import com.kiosk.headquarter.dto.product.HeadProductCreateResponse;
import com.kiosk.headquarter.dto.product.HeadProductInsertDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionCreateRequest;
import com.kiosk.headquarter.dto.product.HeadProductOptionInsertDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionResponse;
import com.kiosk.headquarter.mapper.HeadProductMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HeadProductService {

    private final HeadProductMapper headProductMapper;

    public HeadProductService(HeadProductMapper headProductMapper) {
        this.headProductMapper = headProductMapper;
    }

    public HeadProductCreateResponse createProduct(HeadProductCreateRequest request) {

        validateProductRequest(request);

        int categoryCount = headProductMapper.countCategoryById(request.getCategoryId());

        if (categoryCount == 0) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        if (request.getStoreIds() != null) {
            for (Integer storeId : request.getStoreIds()) {
                int storeCount = headProductMapper.countStoreById(storeId);

                if (storeCount == 0) {
                    throw new IllegalArgumentException("존재하지 않는 지점 ID입니다. storeId=" + storeId);
                }
            }
        }

        HeadProductInsertDTO product = new HeadProductInsertDTO();

        product.setCategoryId(request.getCategoryId());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountRate(request.getDiscountRate() == null ? 0 : request.getDiscountRate());
        product.setIsDisplay(request.getIsDisplay() == null ? true : request.getIsDisplay());

        headProductMapper.insertProduct(product);

        Integer productId = product.getProductId();

        if (request.getOptions() != null) {
            for (HeadProductOptionCreateRequest optionRequest : request.getOptions()) {
                validateOptionRequest(optionRequest);

                HeadProductOptionInsertDTO option = new HeadProductOptionInsertDTO();

                option.setProductId(productId);
                option.setOptionType(optionRequest.getOptionType());
                option.setOptionName(optionRequest.getOptionName());
                option.setExtraPrice(optionRequest.getExtraPrice() == null ? 0 : optionRequest.getExtraPrice());
                option.setMaxFlavorCount(optionRequest.getMaxFlavorCount());

                headProductMapper.insertProductOption(option);
            }
        }

        if (request.getStoreIds() != null) {
            for (Integer storeId : request.getStoreIds()) {
                headProductMapper.insertStoreProduct(productId, storeId);
            }
        }

        HeadProductCreateResponse response = headProductMapper.findProductById(productId);

        List<HeadProductOptionResponse> options =
                headProductMapper.findOptionsByProductId(productId);

        List<Integer> storeIds =
                headProductMapper.findStoreIdsByProductId(productId);

        response.setOptions(options == null ? new ArrayList<>() : options);
        response.setStoreIds(storeIds == null ? new ArrayList<>() : storeIds);

        return response;
    }

    private void validateProductRequest(HeadProductCreateRequest request) {
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리 ID를 입력해야 합니다.");
        }

        if (request.getProductName() == null || request.getProductName().isBlank()) {
            throw new IllegalArgumentException("상품명을 입력해야 합니다.");
        }

        if (request.getBasePrice() == null) {
            throw new IllegalArgumentException("기본 가격을 입력해야 합니다.");
        }

        if (request.getBasePrice() < 0) {
            throw new IllegalArgumentException("기본 가격은 0원 이상이어야 합니다.");
        }

        if (request.getDiscountRate() != null) {
            if (request.getDiscountRate() < 0 || request.getDiscountRate() > 100) {
                throw new IllegalArgumentException("할인율은 0부터 100 사이여야 합니다.");
            }
        }
    }

    private void validateOptionRequest(HeadProductOptionCreateRequest optionRequest) {
        if (optionRequest.getOptionType() == null || optionRequest.getOptionType().isBlank()) {
            throw new IllegalArgumentException("옵션 타입을 입력해야 합니다.");
        }

        if (optionRequest.getOptionName() == null || optionRequest.getOptionName().isBlank()) {
            throw new IllegalArgumentException("옵션명을 입력해야 합니다.");
        }

        if (optionRequest.getExtraPrice() != null && optionRequest.getExtraPrice() < 0) {
            throw new IllegalArgumentException("추가 금액은 0원 이상이어야 합니다.");
        }

        if (optionRequest.getMaxFlavorCount() != null && optionRequest.getMaxFlavorCount() < 0) {
            throw new IllegalArgumentException("최대 맛 선택 개수는 0 이상이어야 합니다.");
        }
    }
}