package com.kiosk.headquarter.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.store.HeadStoreProductAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductUpdateRequestDTO;

@Mapper
public interface HeadStoreProductMapper {

    int insertStoreProduct(
        @Param("storeId") Integer storeId,
        @Param("requestDTO") HeadStoreProductAddRequestDTO requestDTO
    );

    List<HeadStoreProductListResponseDTO> selectStoreProductList(Integer storeId);

    int updateStoreProduct(
        @Param("storeId") Integer storeId,
        @Param("storeProductId") Integer storeProductId,
        @Param("requestDTO") HeadStoreProductUpdateRequestDTO requestDTO
    );
    
    int deleteStoreProduct(
    	    @Param("storeId") Integer storeId,
    	    @Param("storeProductId") Integer storeProductId
    	);
}