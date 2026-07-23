package com.kiosk.customer.flavor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.kiosk.customer.flavor.dto.FlavorResponse;

import java.util.List;

/**
 * [코드 흐름 안내] FlavorServiceImpl
 *
 * <p>역할: 고객 키오스크의 아이스크림 맛 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> FlavorRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class FlavorServiceImpl implements FlavorService {

    private final com.kiosk.customer.flavor.repository.FlavorRepository flavorRepository;

    @Override
    /**
     * [메서드 흐름] getAvailableFlavorsByStore
     * Controller 또는 상위 서비스에서 호출되어 FlavorRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<FlavorResponse> getAvailableFlavorsByStore(Long storeId) {
        List<com.kiosk.entity.StoreFlavor> storeFlavors = flavorRepository.findAvailableFlavorsByStoreId(storeId);
        List<FlavorResponse> responses = new java.util.ArrayList<>();
        for (com.kiosk.entity.StoreFlavor sf : storeFlavors) {
            responses.add(new FlavorResponse(sf.getFlavor().getId(), sf.getFlavor().getFlavorName(), sf.getIsSoldOut(), sf.getFlavor().getImageUrl()));
        }
        return responses;
    }
}