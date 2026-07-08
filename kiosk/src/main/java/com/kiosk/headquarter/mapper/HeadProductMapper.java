package com.kiosk.headquarter.mapper;

import com.kiosk.headquarter.dto.product.HeadProductCreateResponse;
import com.kiosk.headquarter.dto.product.HeadProductInsertDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionInsertDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeadProductMapper {

    int countCategoryById(@Param("categoryId") Integer categoryId);

    int countStoreById(@Param("storeId") Integer storeId);

    int insertProduct(HeadProductInsertDTO product);

    int insertProductOption(HeadProductOptionInsertDTO option);

    int insertStoreProduct(
            @Param("productId") Integer productId,
            @Param("storeId") Integer storeId
    );

    HeadProductCreateResponse findProductById(@Param("productId") Integer productId);

    List<HeadProductOptionResponse> findOptionsByProductId(@Param("productId") Integer productId);

    List<Integer> findStoreIdsByProductId(@Param("productId") Integer productId);
}