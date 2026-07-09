package com.kiosk.customer.flavor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.kiosk.customer.flavor.dto.FlavorResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlavorServiceImpl implements FlavorService {

    // private final StoreFlavorRepository storeFlavorRepository; // 추후 주입 예정

    @Override
    public List<FlavorResponse> getAvailableFlavorsByStore(Long storeId) {
        // TODO: STORE_FLAVORS 테이블과 ICECREAM_FLAVORS 테이블을 조인하여 
        // 해당 지점의 품절 여부(is_sold_out)가 반영된 맛 리스트를 리턴
        return null;
    }
}