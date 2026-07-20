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

@RestController
@RequestMapping("/head/stores/{storeId}/employees")
@RequiredArgsConstructor
public class HeadEmployeeController {

    private final HeadEmployeeService
            headEmployeeService;

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