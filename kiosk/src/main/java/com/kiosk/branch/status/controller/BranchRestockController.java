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

}