package com.kiosk.customer.flavor.service;

import java.util.List;

import com.kiosk.customer.flavor.dto.FlavorResponse;

/**
 * [코드 흐름 안내] FlavorService
 *
 * <p>역할: 고객 키오스크의 아이스크림 맛 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface FlavorService {
    // 특정 매장(storeId)에서 현재 품절되지 않고 판매 중인 맛 목록 조회
    List<FlavorResponse> getAvailableFlavorsByStore(Long storeId);
}