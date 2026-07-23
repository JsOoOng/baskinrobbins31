package com.kiosk.branch.status.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kiosk.branch.status.dto.BranchRestockRequestDTO;
import com.kiosk.branch.status.service.BranchRestockService;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
@RequestMapping("/branch/status/restock")
public class BranchRestockController {



    private final BranchRestockService branchRestockService;




    /*
     * 발주 요청 등록
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
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRestock(
            @PathVariable Integer requestId
    ) {
        String result = branchRestockService.cancelRestock(requestId);
        return ResponseEntity.ok(result);
    }

}