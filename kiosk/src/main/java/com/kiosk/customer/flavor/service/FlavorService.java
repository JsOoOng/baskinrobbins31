package com.kiosk.customer.flavor.service;

import java.util.List;

import com.kiosk.customer.flavor.dto.FlavorResponse;

public interface FlavorService {
    // 특정 매장(storeId)에서 현재 품절되지 않고 판매 중인 맛 목록 조회
    List<FlavorResponse> getAvailableFlavorsByStore(Long storeId);
}