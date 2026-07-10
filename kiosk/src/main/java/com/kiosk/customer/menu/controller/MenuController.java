package com.kiosk.customer.menu.controller; // 👈 패키지 경로는 본인 프로젝트에 맞게 수정하세요!

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/menus")
public class MenuController {

    // 💡 실제 DB 연동(Service/Mapper)을 하기 전에, 
    // 프론트엔드 화면이 잘 나오는지 테스트하기 위해 가짜 데이터를 리턴해 봅니다.
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