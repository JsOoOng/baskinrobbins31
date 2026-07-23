package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.restock.HeadRestockDetailResponseDTO;
import com.kiosk.headquarter.dto.restock.HeadRestockListResponseDTO;
import com.kiosk.headquarter.dto.restock.HeadRestockProcessRequestDTO;
import com.kiosk.headquarter.service.HeadRestockService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadRestockController {

    private final HeadRestockService headRestockService;

    // 발주 요청 전체 목록 조회
    @GetMapping("/head/restocks")
    public List<HeadRestockListResponseDTO> getRestockList() {
        return headRestockService.getRestockList();
    }

    // 승인 대기 발주 요청 목록 조회
    @GetMapping("/head/restocks/waiting")
    public List<HeadRestockListResponseDTO> getWaitingRestockList() {
        return headRestockService.getWaitingRestockList();
    }

    // 발주 요청 상세 조회
    @GetMapping("/head/restocks/{requestId}")
    public HeadRestockDetailResponseDTO getRestockDetail(
            @PathVariable Integer requestId) {

        return headRestockService.getRestockDetail(requestId);
    }

    // 발주 승인
    @PutMapping("/head/restocks/{requestId}/approve")
    public String approveRestock(
            @PathVariable Integer requestId,
            @RequestBody HeadRestockProcessRequestDTO requestDTO) {

        return headRestockService.approveRestock(requestId, requestDTO);
    }

    // 배송 처리
    @PutMapping("/head/restocks/{requestId}/shipping")
    public String startShipping(
            @PathVariable Integer requestId,
            @RequestBody HeadRestockProcessRequestDTO requestDTO) {

        return headRestockService.startShipping(requestId, requestDTO);
    }

    

    // 반려 처리
    @PutMapping("/head/restocks/{requestId}/reject")
    public String rejectRestock(
            @PathVariable Integer requestId,
            @RequestBody HeadRestockProcessRequestDTO requestDTO) {

        return headRestockService.rejectRestock(requestId, requestDTO);
    }
}