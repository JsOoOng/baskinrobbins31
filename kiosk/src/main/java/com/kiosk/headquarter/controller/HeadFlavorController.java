package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.flavor.HeadFlavorCreateRequestDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorResponseDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadFlavorService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadFlavorController
 *
 * <p>역할: 본사 관리의 아이스크림 맛 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadFlavorService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadFlavorController {

    private final HeadFlavorService headFlavorService;

    // 아이스크림 맛 등록
    /**
     * [요청 흐름] POST /head/flavors
     * 프론트 요청을 받아 createFlavor() 메서드가 입력을 받고 HeadFlavorService 호출 후 결과를 응답한다.
     */
    @PostMapping("/head/flavors")
    public HeadFlavorResponseDTO createFlavor(
            @ModelAttribute HeadFlavorCreateRequestDTO requestDTO) {

        return headFlavorService.createFlavor(requestDTO);
    }

    // 아이스크림 맛 전체 목록 조회
    /**
     * [요청 흐름] GET /head/flavors
     * 프론트 요청을 받아 getFlavorList() 메서드가 입력을 받고 HeadFlavorService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/flavors")
    public List<HeadFlavorResponseDTO> getFlavorList() {

        return headFlavorService.getFlavorList();
    }

    // 운영 중인 아이스크림 맛 목록 조회
    /**
     * [요청 흐름] GET /head/flavors/active
     * 프론트 요청을 받아 getActiveFlavorList() 메서드가 입력을 받고 HeadFlavorService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/flavors/active")
    public List<HeadFlavorResponseDTO> getActiveFlavorList() {

        return headFlavorService.getActiveFlavorList();
    }

    // 아이스크림 맛 상세 조회
    /**
     * [요청 흐름] GET /head/flavors/{flavorId}
     * 프론트 요청을 받아 getFlavorDetail() 메서드가 입력을 받고 HeadFlavorService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/flavors/{flavorId}")
    public HeadFlavorResponseDTO getFlavorDetail(
            @PathVariable Integer flavorId) {

        return headFlavorService.getFlavorDetail(flavorId);
    }

    // 아이스크림 맛 수정
    /**
     * [요청 흐름] PUT /head/flavors/{flavorId}
     * 프론트 요청을 받아 updateFlavor() 메서드가 입력을 받고 HeadFlavorService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/flavors/{flavorId}")
    public HeadFlavorResponseDTO updateFlavor(
            @PathVariable Integer flavorId,
            @ModelAttribute HeadFlavorUpdateRequestDTO requestDTO) {

        return headFlavorService.updateFlavor(flavorId, requestDTO);
    }

    // 아이스크림 맛 비활성화
    /**
     * [요청 흐름] DELETE /head/flavors/{flavorId}
     * 프론트 요청을 받아 deleteFlavor() 메서드가 입력을 받고 HeadFlavorService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/head/flavors/{flavorId}")
    public String deleteFlavor(
            @PathVariable Integer flavorId) {

        return headFlavorService.deleteFlavor(flavorId);
    }

}