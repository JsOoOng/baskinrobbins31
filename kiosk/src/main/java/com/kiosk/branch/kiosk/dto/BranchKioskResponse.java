package com.kiosk.branch.kiosk.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Kiosk;
import com.kiosk.entity.enums.KioskStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] BranchKioskResponse
 *
 * <p>역할: 키오스크 기기 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class BranchKioskResponse {

    private Integer kioskId;

    private Integer kioskNumber;

    private String deviceSerial;

    private KioskStatus kioskStatus;

    private LocalDateTime createdAt;

    private Integer storeId;

    private String storeName;

    public static BranchKioskResponse from(Kiosk kiosk){

        return BranchKioskResponse.builder()
                .kioskId(kiosk.getId())
                .kioskNumber(kiosk.getKioskNumber())
                .deviceSerial(kiosk.getDeviceSerial())
                .kioskStatus(kiosk.getKioskStatus())
                .createdAt(kiosk.getCreatedAt())
                .storeId(kiosk.getStore() != null ? kiosk.getStore().getId() : null)
                .storeName(kiosk.getStore() != null ? kiosk.getStore().getStoreName() : null)
                .build();

    }

}