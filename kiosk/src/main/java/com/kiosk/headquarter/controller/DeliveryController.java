package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.headquarter.dto.deliverie.HeadDeliveryResponseDTO;
import com.kiosk.headquarter.service.DeliveryService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/head")
public class DeliveryController {


    private final DeliveryService deliveryService;



    /**
     * 배송 목록 조회
     */
    @GetMapping("/deliveries")
    public List<HeadDeliveryResponseDTO> getDeliveries() {

        return deliveryService.getDeliveries();

    }



   



    /**
     * 배송 상태 변경
     */
    @PutMapping("/delivery/{deliveryId}/status")
    public String changeStatus(
            @PathVariable Integer deliveryId,
            @RequestParam DeliveryStatus status,
            Authentication authentication
    ){

        return deliveryService.changeDeliveryStatus(
                deliveryId,
                status,
                authentication
        );
    }

}