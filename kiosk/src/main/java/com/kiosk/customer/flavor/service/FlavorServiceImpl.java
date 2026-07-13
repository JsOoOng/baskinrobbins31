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
        List<com.kiosk.entity.IcecreamFlavor> flavors = flavorRepository.findAvailableFlavorsByStoreId(storeId);
        List<FlavorResponse> responses = new java.util.ArrayList<>();
        for (com.kiosk.entity.IcecreamFlavor flavor : flavors) {
            responses.add(new FlavorResponse(flavor.getId(), flavor.getFlavorName(), false, flavor.getImageUrl()));
        }
        return responses;
    }
}