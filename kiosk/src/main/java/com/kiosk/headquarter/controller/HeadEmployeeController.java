package com.kiosk.headquarter.controller;

import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateRequest;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateResponse;
import com.kiosk.headquarter.service.HeadEmployeeService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/head/employees")
public class HeadEmployeeController {

    private final HeadEmployeeService headEmployeeService;

    public HeadEmployeeController(HeadEmployeeService headEmployeeService) {
        this.headEmployeeService = headEmployeeService;
    }

    @PostMapping("/store-admins")
    public HeadApiResponse<HeadEmployeeCreateResponse> createStoreAdmin(
            @RequestBody HeadEmployeeCreateRequest request,
            HttpSession session
    ) {
        checkHeadLogin(session);

        HeadEmployeeCreateResponse response =
                headEmployeeService.createStoreAdmin(request);

        return HeadApiResponse.ok("지점 관리자 계정 생성 성공", response);
    }

    private void checkHeadLogin(HttpSession session) {
        Object role = session.getAttribute("HEAD_ROLE");

        if (role == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (!"HEAD_ADMIN".equals(role.toString())
                && !"SUPER_ADMIN".equals(role.toString())) {
            throw new IllegalArgumentException("본사 관리자 권한이 필요합니다.");
        }
        
        
       
    }
}