package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.headquarter.dto.deliverie.HeadDeliveryResponseDTO;
import com.kiosk.headquarter.dto.deliverie.HeadDeliveryCancelRequestDTO;
import com.kiosk.headquarter.service.DeliveryService;

import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] DeliveryController
 *
 * <p>역할: 본사 관리의 배송 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head) -> DeliveryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/head")
public class DeliveryController {


    private final DeliveryService deliveryService;



    /**
     * 배송 목록 조회
     */
    /**
     * [요청 흐름] GET /head/deliveries
     * 프론트 요청을 받아 getDeliveries() 메서드가 입력을 받고 DeliveryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/deliveries")
    public List<HeadDeliveryResponseDTO> getDeliveries() {

        return deliveryService.getDeliveries();

    }



   



    /**
     * 배송 상태 변경
     */
    /**
     * [요청 흐름] PUT /head/delivery/{deliveryId}/status
     * 프론트 요청을 받아 changeStatus() 메서드가 입력을 받고 DeliveryService 호출 후 결과를 응답한다.
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

    /**
     * 쉬운주석: 배송 취소 모달의 사유를 받아 배송과 재고 신청을 함께 취소 처리한다.
     */
    @PutMapping("/delivery/{deliveryId}/cancel")
    public String cancelDelivery(
            @PathVariable Integer deliveryId,
            @RequestBody HeadDeliveryCancelRequestDTO requestDTO,
            Authentication authentication
    ) {
        return deliveryService.cancelDelivery(
                deliveryId,
                requestDTO.getReason(),
                authentication
        );
    }

}
