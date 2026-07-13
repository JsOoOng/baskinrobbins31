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

@RestController
@RequiredArgsConstructor
public class HeadStoreFlavorController {

    private final HeadStoreFlavorService headStoreFlavorService;

    // 지점별 맛 배정
    @PostMapping("/head/stores/{storeId}/flavors")
    public String addStoreFlavor(
            @PathVariable Integer storeId,
            @RequestBody HeadStoreFlavorAddRequestDTO requestDTO) {

        return headStoreFlavorService.addStoreFlavor(storeId, requestDTO);
    }

    // 지점별 맛 목록 조회
    @GetMapping("/head/stores/{storeId}/flavors")
    public List<HeadStoreFlavorListResponseDTO> getStoreFlavorList(
            @PathVariable Integer storeId) {

        return headStoreFlavorService.getStoreFlavorList(storeId);
    }

    // 지점별 맛 상세 조회
    @GetMapping("/head/stores/{storeId}/flavors/{storeFlavorId}")
    public HeadStoreFlavorDetailResponseDTO getStoreFlavorDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer storeFlavorId) {

        return headStoreFlavorService.getStoreFlavorDetail(storeId, storeFlavorId);
    }

    // 지점별 맛 통 개수 / 품절 여부 수정
    @PutMapping("/head/stores/{storeId}/flavors/{storeFlavorId}")
    public String updateStoreFlavor(
            @PathVariable Integer storeId,
            @PathVariable Integer storeFlavorId,
            @RequestBody HeadStoreFlavorUpdateRequestDTO requestDTO) {

        return headStoreFlavorService.updateStoreFlavor(storeId, storeFlavorId, requestDTO);
    }

    // 지점별 맛 배정 삭제
    @DeleteMapping("/head/stores/{storeId}/flavors/{storeFlavorId}")
    public String deleteStoreFlavor(
            @PathVariable Integer storeId,
            @PathVariable Integer storeFlavorId) {

        return headStoreFlavorService.deleteStoreFlavor(storeId, storeFlavorId);
    }
}