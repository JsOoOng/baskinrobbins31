package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorDetailResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreFlavorUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadFlavorMapper;
import com.kiosk.headquarter.repository.HeadStoreFlavorMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreFlavorService {

    private final HeadStoreFlavorMapper headStoreFlavorMapper;
    private final HeadStoreMapper headStoreMapper;
    private final HeadFlavorMapper headFlavorMapper;

    // 지점별 맛 배정
    @Transactional
    public String addStoreFlavor(Integer storeId, HeadStoreFlavorAddRequestDTO requestDTO) {

        Store store = headStoreMapper.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점입니다."));

        if (requestDTO.getFlavorId() == null) {
            throw new IllegalArgumentException("배정할 맛을 선택해주세요.");
        }

        IcecreamFlavor flavor = headFlavorMapper.findById(requestDTO.getFlavorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 맛입니다."));

        if (Boolean.FALSE.equals(flavor.getIsActive())) {
            throw new IllegalArgumentException("비활성화된 맛은 지점에 배정할 수 없습니다.");
        }

        boolean alreadyExists = headStoreFlavorMapper.existsByStore_IdAndFlavor_Id(
                storeId,
                requestDTO.getFlavorId()
        );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 해당 지점에 등록된 맛입니다.");
        }

        StoreFlavor storeFlavor = StoreFlavor.builder()
                .store(store)
                .flavor(flavor)
                .container(
                        requestDTO.getContainer() != null
                                ? requestDTO.getContainer()
                                : 0
                )
                .isSoldOut(
                        requestDTO.getIsSoldOut() != null
                                ? requestDTO.getIsSoldOut()
                                : false
                )
                .build();

        headStoreFlavorMapper.save(storeFlavor);

        return "지점 맛 등록 성공";
    }

    // 지점별 맛 목록 조회
    public List<HeadStoreFlavorListResponseDTO> getStoreFlavorList(Integer storeId) {

        headStoreMapper.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점입니다."));

        return headStoreFlavorMapper.findByStore_IdOrderByIdDesc(storeId)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 지점별 맛 상세 조회
    public HeadStoreFlavorDetailResponseDTO getStoreFlavorDetail(
            Integer storeId,
            Integer storeFlavorId) {

        StoreFlavor storeFlavor = headStoreFlavorMapper.findByStore_IdAndId(storeId, storeFlavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 맛 정보입니다."));

        return toDetailResponseDTO(storeFlavor);
    }

    // 지점별 맛 품절 여부 / 통 개수 수정
    @Transactional
    public String updateStoreFlavor(
            Integer storeId,
            Integer storeFlavorId,
            HeadStoreFlavorUpdateRequestDTO requestDTO) {

        StoreFlavor storeFlavor = headStoreFlavorMapper.findByStore_IdAndId(storeId, storeFlavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 맛 정보입니다."));

        Boolean nextSoldOut = requestDTO.getIsSoldOut() != null
                ? requestDTO.getIsSoldOut()
                : storeFlavor.getIsSoldOut();

        Integer nextContainer = requestDTO.getContainer() != null
                ? requestDTO.getContainer()
                : storeFlavor.getContainer();

        storeFlavor.updateStoreFlavor(nextSoldOut, nextContainer);

        return "지점 맛 정보 수정 성공";
    }

    // 지점별 맛 배정 삭제
    @Transactional
    public String deleteStoreFlavor(Integer storeId, Integer storeFlavorId) {

        StoreFlavor storeFlavor = headStoreFlavorMapper.findByStore_IdAndId(storeId, storeFlavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 맛 정보입니다."));

        headStoreFlavorMapper.delete(storeFlavor);

        return "지점 맛 삭제 성공";
    }

    private HeadStoreFlavorListResponseDTO toListResponseDTO(StoreFlavor storeFlavor) {

        return HeadStoreFlavorListResponseDTO.builder()
                .storeFlavorId(storeFlavor.getId())
                .storeId(storeFlavor.getStore().getId())
                .flavorId(storeFlavor.getFlavor().getId())
                .flavorName(storeFlavor.getFlavor().getFlavorName())
                .flavorIsActive(storeFlavor.getFlavor().getIsActive())
                .container(storeFlavor.getContainer())
                .isSoldOut(storeFlavor.getIsSoldOut())
                .build();
    }

    private HeadStoreFlavorDetailResponseDTO toDetailResponseDTO(StoreFlavor storeFlavor) {

        return HeadStoreFlavorDetailResponseDTO.builder()
                .storeFlavorId(storeFlavor.getId())
                .storeId(storeFlavor.getStore().getId())
                .storeName(storeFlavor.getStore().getStoreName())
                .flavorId(storeFlavor.getFlavor().getId())
                .flavorName(storeFlavor.getFlavor().getFlavorName())
                .flavorIsActive(storeFlavor.getFlavor().getIsActive())
                .container(storeFlavor.getContainer())
                .isSoldOut(storeFlavor.getIsSoldOut())
                .build();
    }
}