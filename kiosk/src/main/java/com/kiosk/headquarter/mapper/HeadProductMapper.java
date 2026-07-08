package com.kiosk.headquarter.mapper;

import com.kiosk.headquarter.dto.product.HeadProductCreateResponse;
import com.kiosk.headquarter.dto.product.HeadProductInsertDTO;
import com.kiosk.headquarter.dto.product.HeadProductListResponseDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionInsertDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionResponse;
import com.kiosk.headquarter.dto.product.HeadProductDetailResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.kiosk.headquarter.dto.product.HeadProductUpdateRequestDTO;

import java.util.List;

@Mapper
public interface HeadProductMapper {

    int countCategoryById(@Param("categoryId") Integer categoryId);

    int countStoreById(@Param("storeId") Integer storeId);

    int insertProduct(HeadProductInsertDTO product);

    int insertProductOption(HeadProductOptionInsertDTO option);

    int insertStoreProduct(
            @Param("productId") Integer productId,
            @Param("storeId") Integer storeId);
    
    int updateProduct(
    	    @Param("productId") Integer productId,
    	    @Param("updateDTO") HeadProductUpdateRequestDTO updateDTO);
    
    int deleteProduct(Integer productId);
    
    HeadProductCreateResponse findProductById(@Param("productId") Integer productId);
    
    HeadProductDetailResponseDTO selectProductDetail(Integer productId);

    List<HeadProductOptionResponse> findOptionsByProductId(@Param("productId") Integer productId);
    
    List<HeadProductListResponseDTO> selectProductList();

    List<Integer> findStoreIdsByProductId(@Param("productId") Integer productId);
}