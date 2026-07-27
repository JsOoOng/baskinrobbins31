package com.kiosk.branch.status.dto;

import com.kiosk.entity.StoreProduct;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] StoreProductStatusResponse
 *
 * <p>역할: 지점 판매 상품 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class StoreProductStatusResponse {


    private Integer storeProductId;


    /*
     * 발주용 재고 ID
     */
    private Integer storeInventoryId;


    private Integer productId;


    private String productName;


    /*
     * 최종 품절 상태
     * manualSoldOut || 재고 0
     */
    private Boolean soldOut;


    private Integer currentStock;




    public static StoreProductStatusResponse from(
            StoreProduct sp,
            Integer storeInventoryId,
            Integer currentStock,
            Boolean soldOut
    ){

        return StoreProductStatusResponse.builder()

                .storeProductId(
                        sp.getId()
                )

                .storeInventoryId(
                        storeInventoryId
                )

                .productId(
                        sp.getProduct().getId()
                )

                .productName(
                        sp.getProduct().getProductName()
                )

                .soldOut(
                        soldOut
                )

                .currentStock(
                        currentStock
                )

                .build();

    }

}