package com.kiosk.customer.menu.controller; // 👈 패키지 경로는 본인 프로젝트에 맞게 수정하세요!

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * [코드 흐름 안내] MenuController
 *
 * <p>역할: 고객 키오스크의 고객 메뉴 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/customer/menus) -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/customer/menus")
public class MenuController {

    // 💡 실제 DB 연동(Service/Mapper)을 하기 전에, 
    // 프론트엔드 화면이 잘 나오는지 테스트하기 위해 가짜 데이터를 리턴해 봅니다.
    /**
     * [요청 흐름] GET /api/customer/menus
     * 프론트 요청을 받아 getMenus() 메서드가 입력을 검증하고 처리 결과를 응답한다.
     */
    @GetMapping
    public List<Map<String, Object>> getMenus() {
        return List.of(
            Map.of("productId", 1, "productName", "싱글레귤러", "basePrice", 3500),
            Map.of("productId", 2, "productName", "파인트", "basePrice", 9800),
            Map.of("productId", 3, "productName", "쿼터", "basePrice", 18500),
            Map.of("productId", 4, "productName", "아이스크림 케이크", "basePrice", 25000)
        );
    }
}