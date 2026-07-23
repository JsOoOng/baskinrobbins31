package com.kiosk.customer.flavor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.kiosk.customer.flavor.dto.FlavorResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlavorServiceImpl implements FlavorService {

    private final com.kiosk.customer.flavor.repository.FlavorRepository flavorRepository;

    @Override
    public List<FlavorResponse> getAvailableFlavorsByStore(Long storeId) {
        List<com.kiosk.entity.StoreFlavor> storeFlavors = flavorRepository.findAvailableFlavorsByStoreId(storeId);
        List<FlavorResponse> responses = new java.util.ArrayList<>();
        for (com.kiosk.entity.StoreFlavor sf : storeFlavors) {
            responses.add(new FlavorResponse(sf.getFlavor().getId(), sf.getFlavor().getFlavorName(), sf.getIsSoldOut(), sf.getFlavor().getImageUrl()));
        }
        return responses;
    }
}