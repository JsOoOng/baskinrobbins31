package com.kiosk.branch.expense.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.expense.dto.StoreExpenseRequestDTO;
import com.kiosk.branch.expense.service.StoreExpenseService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/branch/expense")
public class BranchExpenseController {


    private final StoreExpenseService storeExpenseService;



    /*
     * 지출 등록
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

}