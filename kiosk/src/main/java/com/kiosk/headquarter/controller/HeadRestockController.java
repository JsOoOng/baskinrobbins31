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

/**
 * [코드 흐름 안내] HeadRestockController
 *
 * <p>역할: 본사 관리의 재입고·발주 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadRestockService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadRestockController {

    private final HeadRestockService headRestockService;

    // 발주 요청 전체 목록 조회
    /**
     * [요청 흐름] GET /head/restocks
     * 프론트 요청을 받아 getRestockList() 메서드가 입력을 받고 HeadRestockService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/restocks")
    public List<HeadRestockListResponseDTO> getRestockList() {
        return headRestockService.getRestockList();
    }

    // 승인 대기 발주 요청 목록 조회
    /**
     * [요청 흐름] GET /head/restocks/waiting
     * 프론트 요청을 받아 getWaitingRestockList() 메서드가 입력을 받고 HeadRestockService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/restocks/waiting")
    public List<HeadRestockListResponseDTO> getWaitingRestockList() {
        return headRestockService.getWaitingRestockList();
    }

    // 발주 요청 상세 조회
    /**
     * [요청 흐름] GET /head/restocks/{requestId}
     * 프론트 요청을 받아 getRestockDetail() 메서드가 입력을 받고 HeadRestockService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/restocks/{requestId}")
    public HeadRestockDetailResponseDTO getRestockDetail(
            @PathVariable Integer requestId) {

        return headRestockService.getRestockDetail(requestId);
    }

    // 발주 승인
    /**
     * [요청 흐름] PUT /head/restocks/{requestId}/approve
     * 프론트 요청을 받아 approveRestock() 메서드가 입력을 받고 HeadRestockService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/restocks/{requestId}/approve")
    public String approveRestock(
            @PathVariable Integer requestId,
            @RequestBody HeadRestockProcessRequestDTO requestDTO) {

        return headRestockService.approveRestock(requestId, requestDTO);
    }

    // 배송 처리
    /**
     * [요청 흐름] PUT /head/restocks/{requestId}/shipping
     * 프론트 요청을 받아 startShipping() 메서드가 입력을 받고 HeadRestockService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/restocks/{requestId}/shipping")
    public String startShipping(
            @PathVariable Integer requestId,
            @RequestBody HeadRestockProcessRequestDTO requestDTO) {

        return headRestockService.startShipping(requestId, requestDTO);
    }

    

    // 반려 처리
    /**
     * [요청 흐름] PUT /head/restocks/{requestId}/reject
     * 프론트 요청을 받아 rejectRestock() 메서드가 입력을 받고 HeadRestockService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/restocks/{requestId}/reject")
    public String rejectRestock(
            @PathVariable Integer requestId,
            @RequestBody HeadRestockProcessRequestDTO requestDTO) {

        return headRestockService.rejectRestock(requestId, requestDTO);
    }
}