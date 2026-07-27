package com.kiosk.headquarter.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateRequest;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateResponse;
import com.kiosk.headquarter.service.HeadEmployeeService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadEmployeeController
 *
 * <p>역할: 본사 관리의 직원 계정 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/stores/{storeId}/employees) -> HeadEmployeeService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/stores/{storeId}/employees")
@RequiredArgsConstructor
public class HeadEmployeeController {

    private final HeadEmployeeService
            headEmployeeService;

    /**
     * [요청 흐름] POST /head/stores/{storeId}/employees
     * 프론트 요청을 받아 createStoreManager() 메서드가 입력을 받고 HeadEmployeeService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public HeadApiResponse<HeadEmployeeCreateResponse>
            createStoreManager(
                    @PathVariable
                    Integer storeId,

                    @RequestBody
                    HeadEmployeeCreateRequest request
            ) {

        request.setStoreId(storeId);

        HeadEmployeeCreateResponse response =
                headEmployeeService
                        .createStoreManager(request);

        return HeadApiResponse.ok(
                "지점 관리자 계정 생성 성공",
                response
        );
    }
}