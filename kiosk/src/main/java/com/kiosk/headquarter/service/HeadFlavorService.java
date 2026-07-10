package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.headquarter.dto.flavor.HeadFlavorCreateRequestDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorResponseDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadFlavorMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadFlavorService {

    private final HeadFlavorMapper headFlavorMapper;

    // 아이스크림 맛 등록
    @Transactional
    public HeadFlavorResponseDTO createFlavor(HeadFlavorCreateRequestDTO requestDTO) {

        if (requestDTO.getFlavorName() == null || requestDTO.getFlavorName().isBlank()) {
            throw new IllegalArgumentException("맛 이름을 입력해주세요.");
        }

        boolean alreadyExists = headFlavorMapper.existsByFlavorName(requestDTO.getFlavorName());

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 맛 이름입니다.");
        }

        IcecreamFlavor flavor = IcecreamFlavor.builder()
                .flavorName(requestDTO.getFlavorName())
                .isActive(
                        requestDTO.getIsActive() != null
                                ? requestDTO.getIsActive()
                                : true
                )
                .imageUrl(requestDTO.getImageUrl())
                .build();

        IcecreamFlavor savedFlavor = headFlavorMapper.save(flavor);

        return toResponseDTO(savedFlavor);
    }

    // 아이스크림 맛 전체 목록 조회
    public List<HeadFlavorResponseDTO> getFlavorList() {

        return headFlavorMapper.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 운영 중인 아이스크림 맛 목록 조회
    public List<HeadFlavorResponseDTO> getActiveFlavorList() {

        return headFlavorMapper.findByIsActiveTrueOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 아이스크림 맛 상세 조회
    public HeadFlavorResponseDTO getFlavorDetail(Integer flavorId) {

        IcecreamFlavor flavor = headFlavorMapper.findById(flavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 맛입니다."));

        return toResponseDTO(flavor);
    }

    // 아이스크림 맛 수정
    @Transactional
    public HeadFlavorResponseDTO updateFlavor(
            Integer flavorId,
            HeadFlavorUpdateRequestDTO requestDTO) {

        IcecreamFlavor flavor = headFlavorMapper.findById(flavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 맛입니다."));

        if (requestDTO.getFlavorName() == null || requestDTO.getFlavorName().isBlank()) {
            throw new IllegalArgumentException("맛 이름을 입력해주세요.");
        }

        boolean alreadyExists = headFlavorMapper.existsByFlavorNameAndIdNot(
                requestDTO.getFlavorName(),
                flavorId
        );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 맛 이름입니다.");
        }

        flavor.updateFlavor(
                requestDTO.getFlavorName(),
                requestDTO.getIsActive() != null
                        ? requestDTO.getIsActive()
                        : flavor.getIsActive(),
                requestDTO.getImageUrl()
        );

        return toResponseDTO(flavor);
    }

    // 아이스크림 맛 비활성화
    @Transactional
    public String deleteFlavor(Integer flavorId) {

        IcecreamFlavor flavor = headFlavorMapper.findById(flavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 맛입니다."));

        flavor.deactivate();

        return "아이스크림 맛 비활성화 성공";
    }

    private HeadFlavorResponseDTO toResponseDTO(IcecreamFlavor flavor) {

        return HeadFlavorResponseDTO.builder()
                .flavorId(flavor.getId())
                .flavorName(flavor.getFlavorName())
                .isActive(flavor.getIsActive())
                .imageUrl(flavor.getImageUrl())
                .build();
    }
}