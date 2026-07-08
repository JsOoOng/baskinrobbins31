package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.dto.product.HeadProductCreateRequest;
import com.kiosk.headquarter.dto.product.HeadProductCreateResponse;
import com.kiosk.headquarter.dto.product.HeadProductListResponseDTO;
import com.kiosk.headquarter.dto.product.HeadProductUpdateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductDetailResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.kiosk.headquarter.service.HeadProductService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor

public class HeadProductController {

    private final HeadProductService headProductService;

    @GetMapping("/head/products")
    public List<HeadProductListResponseDTO> getProductList() {
        return headProductService.getProductList();
    }
    
    @GetMapping("/head/products/{productId}")
    public HeadProductDetailResponseDTO getProductDetail(@PathVariable Integer productId) {
        return headProductService.getProductDetail(productId);
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
    
    @PutMapping("/head/products/{productId}")
    public String updateProduct(
            @PathVariable Integer productId,
            @RequestBody HeadProductUpdateRequestDTO updateDTO) {

        boolean result = headProductService.updateProduct(productId, updateDTO);

        if (result) {
            return "메뉴 수정 성공";
        }

        return "메뉴 수정 실패";
    }
    
    @DeleteMapping("/head/products/{productId}")
    public String deleteProduct(@PathVariable Integer productId) {

        boolean result = headProductService.deleteProduct(productId);

        if (result) {
            return "메뉴 삭제 성공";
        }

        return "메뉴 삭제 실패";
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