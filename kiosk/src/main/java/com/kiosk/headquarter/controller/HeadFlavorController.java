package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.flavor.HeadFlavorCreateRequestDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorResponseDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadFlavorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadFlavorController {

    private final HeadFlavorService headFlavorService;

    // 아이스크림 맛 등록
    @PostMapping("/head/flavors")
    public HeadFlavorResponseDTO createFlavor(
            @RequestBody HeadFlavorCreateRequestDTO requestDTO) {

        return headFlavorService.createFlavor(requestDTO);
    }

    // 아이스크림 맛 전체 목록 조회
    @GetMapping("/head/flavors")
    public List<HeadFlavorResponseDTO> getFlavorList() {

        return headFlavorService.getFlavorList();
    }

    // 운영 중인 아이스크림 맛 목록 조회
    @GetMapping("/head/flavors/active")
    public List<HeadFlavorResponseDTO> getActiveFlavorList() {

        return headFlavorService.getActiveFlavorList();
    }

    // 아이스크림 맛 상세 조회
    @GetMapping("/head/flavors/{flavorId}")
    public HeadFlavorResponseDTO getFlavorDetail(
            @PathVariable Integer flavorId) {

        return headFlavorService.getFlavorDetail(flavorId);
    }

    // 아이스크림 맛 수정
    @PutMapping("/head/flavors/{flavorId}")
    public HeadFlavorResponseDTO updateFlavor(
            @PathVariable Integer flavorId,
            @RequestBody HeadFlavorUpdateRequestDTO requestDTO) {

        return headFlavorService.updateFlavor(flavorId, requestDTO);
    }

    // 아이스크림 맛 비활성화
    @DeleteMapping("/head/flavors/{flavorId}")
    public String deleteFlavor(
            @PathVariable Integer flavorId) {

        return headFlavorService.deleteFlavor(flavorId);
    }
}