package com.kiosk.branch.parttime.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.parttime.dto.SalaryPaymentRequestDTO;
import com.kiosk.branch.parttime.dto.SalaryResponseDTO;
import com.kiosk.branch.parttime.service.BranchSalaryService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/branch/parttime")
@RequiredArgsConstructor
public class BranchSalaryController {


    private final BranchSalaryService salaryService;



    /*
     * 직원 급여 조회
     *
     * GET
     * /branch/parttime/staff/{staffId}/salary
     *
     * ?year=2026&month=7
     */
    @GetMapping(
            "/staff/{staffId}/salary"
    )
    public SalaryResponseDTO getSalary(
            @PathVariable Integer staffId,

            @RequestParam Integer year,

            @RequestParam Integer month
    ){


        return salaryService.calculateSalary(
                staffId,
                year,
                month
        );

    }





    /*
     * 급여 지급 처리
     *
     * 계산된 급여를
     * STORE_EXPENSES 에 저장
     *
     * POST
     * /branch/parttime/staff/{staffId}/salary/pay
     */
    @PostMapping(
            "/staff/{staffId}/salary/pay"
    )
    public void paySalary(
            @PathVariable Integer staffId,

            @RequestBody SalaryPaymentRequestDTO requestDTO
    ){


        salaryService.paySalary(
                staffId,
                requestDTO
        );


    }


}