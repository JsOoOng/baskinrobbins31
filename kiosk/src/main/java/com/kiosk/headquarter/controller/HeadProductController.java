package com.kiosk.headquarter.controller;

import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.dto.product.HeadProductCreateRequest;
import com.kiosk.headquarter.dto.product.HeadProductCreateResponse;
import com.kiosk.headquarter.service.HeadProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/head/products")
public class HeadProductController {

    private final HeadProductService headProductService;

    public HeadProductController(HeadProductService headProductService) {
        this.headProductService = headProductService;
    }

    @PostMapping
    public HeadApiResponse<HeadProductCreateResponse> createProduct(
            @RequestBody HeadProductCreateRequest request,
            HttpSession session
    ) {
        checkHeadLogin(session);

        HeadProductCreateResponse response =
                headProductService.createProduct(request);

        return HeadApiResponse.ok("메뉴 추가 성공", response);
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