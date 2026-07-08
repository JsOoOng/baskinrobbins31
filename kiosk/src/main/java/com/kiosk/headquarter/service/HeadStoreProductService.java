package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kiosk.headquarter.dto.store.HeadStoreProductAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductUpdateRequestDTO;
import com.kiosk.headquarter.mapper.HeadStoreProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeadStoreProductService {

    private final HeadStoreProductMapper headStoreProductMapper;

    public boolean addStoreProduct(Integer storeId, HeadStoreProductAddRequestDTO requestDTO) {
        int result = headStoreProductMapper.insertStoreProduct(storeId, requestDTO);
        return result > 0;
    }
    
    public boolean updateStoreProduct(
            Integer storeId,
            Integer storeProductId,
            HeadStoreProductUpdateRequestDTO requestDTO) {

        int result = headStoreProductMapper.updateStoreProduct(storeId, storeProductId, requestDTO);
        return result > 0;
    }
    
    public List<HeadStoreProductListResponseDTO> getStoreProductList(Integer storeId) {
        return headStoreProductMapper.selectStoreProductList(storeId);
    }

    public boolean deleteStoreProduct(Integer storeId, Integer storeProductId) {
        int result = headStoreProductMapper.deleteStoreProduct(storeId, storeProductId);
        return result > 0;
    }
    
}