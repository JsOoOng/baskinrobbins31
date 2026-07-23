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


/**
 * [코드 흐름 안내] BranchSalaryController
 *
 * <p>역할: 지점 운영의 급여 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/parttime) -> BranchSalaryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    /**
     * [요청 흐름] GET /branch/parttime
     * 프론트 요청을 받아 getSalary() 메서드가 입력을 받고 BranchSalaryService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] POST /branch/parttime
     * 프론트 요청을 받아 paySalary() 메서드가 입력을 받고 BranchSalaryService 호출 후 결과를 응답한다.
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