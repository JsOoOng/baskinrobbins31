package com.kiosk.branch.status.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kiosk.branch.status.dto.BranchRestockRequestDTO;
import com.kiosk.branch.status.service.BranchRestockService;

import lombok.RequiredArgsConstructor;



/**
 * [코드 흐름 안내] BranchRestockController
 *
 * <p>역할: 지점 운영의 재입고·발주 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/status/restock) -> BranchRestockService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/branch/status/restock")
public class BranchRestockController {



    private final BranchRestockService branchRestockService;




    /*
     * 발주 요청 등록
     */
    /**
     * [요청 흐름] POST /branch/status/restock
     * 프론트 요청을 받아 requestRestock() 메서드가 입력을 받고 BranchRestockService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<?> requestRestock(
            @RequestBody BranchRestockRequestDTO dto
    ) {


        String result =
                branchRestockService
                .requestRestock(dto);



        return ResponseEntity.ok(
                result
        );
    }

    /*
     * 지점별 발주 요청 내역 조회
     */
    /**
     * [요청 흐름] GET /branch/status/restock/{storeId}
     * 프론트 요청을 받아 getRestockList() 메서드가 입력을 받고 BranchRestockService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<?> getRestockList(
            @PathVariable Integer storeId
    ) {
        java.util.List<com.kiosk.branch.status.dto.BranchRestockListResponseDTO> result = branchRestockService.getRestockList(storeId);
        return ResponseEntity.ok(result);
    }

    /*
     * 발주 요청 취소
     */
    /**
     * [요청 흐름] POST /branch/status/restock/{requestId}/cancel
     * 프론트 요청을 받아 cancelRestock() 메서드가 입력을 받고 BranchRestockService 호출 후 결과를 응답한다.
     */
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRestock(
            @PathVariable Integer requestId
    ) {
        String result = branchRestockService.cancelRestock(requestId);
        return ResponseEntity.ok(result);
    }

}