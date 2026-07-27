package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.store.HeadStoreFlavorAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorDetailResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadStoreFlavorService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadStoreFlavorController
 *
 * <p>역할: 본사 관리의 지점별 맛 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadStoreFlavorService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadStoreFlavorController {

    private final HeadStoreFlavorService headStoreFlavorService;

    // 지점별 맛 배정
    /**
     * [요청 흐름] POST /head/stores/{storeId}/flavors
     * 프론트 요청을 받아 addStoreFlavor() 메서드가 입력을 받고 HeadStoreFlavorService 호출 후 결과를 응답한다.
     */
    @PostMapping("/head/stores/{storeId}/flavors")
    public String addStoreFlavor(
            @PathVariable Integer storeId,
            @RequestBody HeadStoreFlavorAddRequestDTO requestDTO) {

        return headStoreFlavorService.addStoreFlavor(storeId, requestDTO);
    }

    // 지점별 맛 목록 조회
    /**
     * [요청 흐름] GET /head/stores/{storeId}/flavors
     * 프론트 요청을 받아 getStoreFlavorList() 메서드가 입력을 받고 HeadStoreFlavorService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/stores/{storeId}/flavors")
    public List<HeadStoreFlavorListResponseDTO> getStoreFlavorList(
            @PathVariable Integer storeId) {

        return headStoreFlavorService.getStoreFlavorList(storeId);
    }

    // 지점별 맛 상세 조회
    /**
     * [요청 흐름] GET /head/stores/{storeId}/flavors/{storeFlavorId}
     * 프론트 요청을 받아 getStoreFlavorDetail() 메서드가 입력을 받고 HeadStoreFlavorService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/stores/{storeId}/flavors/{storeFlavorId}")
    public HeadStoreFlavorDetailResponseDTO getStoreFlavorDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer storeFlavorId) {

        return headStoreFlavorService.getStoreFlavorDetail(storeId, storeFlavorId);
    }

    // 지점별 맛 통 개수 / 품절 여부 수정
    /**
     * [요청 흐름] PUT /head/stores/{storeId}/flavors/{storeFlavorId}
     * 프론트 요청을 받아 updateStoreFlavor() 메서드가 입력을 받고 HeadStoreFlavorService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/stores/{storeId}/flavors/{storeFlavorId}")
    public String updateStoreFlavor(
            @PathVariable Integer storeId,
            @PathVariable Integer storeFlavorId,
            @RequestBody HeadStoreFlavorUpdateRequestDTO requestDTO) {

        return headStoreFlavorService.updateStoreFlavor(storeId, storeFlavorId, requestDTO);
    }

    // 지점별 맛 배정 삭제
    /**
     * [요청 흐름] DELETE /head/stores/{storeId}/flavors/{storeFlavorId}
     * 프론트 요청을 받아 deleteStoreFlavor() 메서드가 입력을 받고 HeadStoreFlavorService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/head/stores/{storeId}/flavors/{storeFlavorId}")
    public String deleteStoreFlavor(
            @PathVariable Integer storeId,
            @PathVariable Integer storeFlavorId) {

        return headStoreFlavorService.deleteStoreFlavor(storeId, storeFlavorId);
    }
}