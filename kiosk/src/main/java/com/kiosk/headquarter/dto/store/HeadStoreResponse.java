package com.kiosk.headquarter.dto.store;

import java.time.LocalDateTime;

import com.kiosk.entity.Store;
import com.kiosk.entity.enums.StoreStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadStoreResponse
 *
 * <p>역할: 지점 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class HeadStoreResponse {

    private Integer storeId;

    private String storeName;

    private String businessNumber;

    private String region;

    private String address;

    private String phone;

    private String businessHours;

    private StoreStatus storeStatus;

    private LocalDateTime createdAt;

    /*
     * Store 엔티티를 응답 DTO로 변환
     */
    public static HeadStoreResponse from(
            Store store
    ) {

        return HeadStoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .businessNumber(
                        store.getBusinessNumber()
                )
                .region(store.getRegion())
                .address(store.getAddress())
                .phone(store.getPhone())
                .businessHours(
                        store.getBusinessHours()
                )
                .storeStatus(
                        store.getStoreStatus()
                )
                .createdAt(store.getCreatedAt())
                .build();
    }
}