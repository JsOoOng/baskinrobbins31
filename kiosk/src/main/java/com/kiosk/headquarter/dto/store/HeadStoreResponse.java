package com.kiosk.headquarter.dto.store;

import java.time.LocalDateTime;

import com.kiosk.entity.Store;
import com.kiosk.entity.enums.StoreStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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