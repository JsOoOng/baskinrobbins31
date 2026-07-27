package com.kiosk.headquarter.dto.storeFlavor;

import java.time.LocalDateTime;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * [코드 흐름 안내] HeadStoreFlavorInventoryResponse
 *
 * <p>역할: 재고 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class HeadStoreFlavorInventoryResponse {


    /*
     * 지점 맛 재고 ID
     */
    private Integer storeFlavorId;


    /*
     * 지점 정보
     */
    private Integer storeId;

    private String storeName;


    /*
     * 맛 정보
     */
    private Integer flavorId;

    private String flavorName;


    /*
     * 현재 보유량(Container)
     */
    private Integer container;


    /*
     * 품절 여부
     */
    private Boolean isSoldOut;


    /*
     * 자동 보충 설정
     */
    private Integer minStock;

    private Integer targetStock;

    private Boolean autoRestockEnabled;

    private AutoRestockMode restockMode;


    /*
     * 마지막 수정 시간
     */
    private LocalDateTime lastUpdated;



    public static HeadStoreFlavorInventoryResponse from(
            StoreFlavor storeFlavor
    ) {

        return HeadStoreFlavorInventoryResponse.builder()

                .storeFlavorId(
                        storeFlavor.getId()
                )

                .storeId(
                        storeFlavor.getStore().getId()
                )

                .storeName(
                        storeFlavor.getStore().getStoreName()
                )

                .flavorId(
                        storeFlavor.getFlavor().getId()
                )

                .flavorName(
                        storeFlavor.getFlavor().getFlavorName()
                )

                .container(
                        storeFlavor.getContainer()
                )

                .isSoldOut(
                        storeFlavor.getIsSoldOut()
                )

                .minStock(
                        storeFlavor.getMinStock()
                )

                .targetStock(
                        storeFlavor.getTargetStock()
                )

                .autoRestockEnabled(
                        storeFlavor.getAutoRestockEnabled()
                )

                .restockMode(
                        storeFlavor.getRestockMode()
                )

                .lastUpdated(
                        storeFlavor.getLastUpdated()
                )

                .build();
    }
}