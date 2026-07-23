package com.kiosk.branch.expense.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.expense.dto.ExpenseHistoryResponseDTO;
import com.kiosk.branch.expense.dto.StoreExpenseRequestDTO;
import com.kiosk.branch.expense.service.StoreExpenseService;

import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] BranchExpenseController
 *
 * <p>역할: 지점 운영의 지출 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/expense) -> StoreExpenseService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/branch/expense")
public class BranchExpenseController {


    private final StoreExpenseService storeExpenseService;



    /*
     * 지출 등록
     */
    /**
     * [요청 흐름] POST /branch/expense
     * 프론트 요청을 받아 createExpense() 메서드가 입력을 받고 StoreExpenseService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<String> createExpense(
            @RequestBody StoreExpenseRequestDTO dto
    ) {


        storeExpenseService.createExpense(dto);


        return ResponseEntity.ok(
                "지출 등록 완료"
        );
    }
    
    /*
     * 지출 자동완성 목록 조회
     */
    /**
     * [요청 흐름] GET /branch/expense/history
     * 프론트 요청을 받아 getExpenseHistory() 메서드가 입력을 받고 StoreExpenseService 호출 후 결과를 응답한다.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ExpenseHistoryResponseDTO>> getExpenseHistory(
            @RequestParam Integer storeId
    ) {

        return ResponseEntity.ok(
                storeExpenseService.getExpenseHistory(storeId)
        );

    }

}