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

/**
 * [코드 흐름 안내] HeadStoreFlavorService
 *
 * <p>역할: 본사 관리의 지점별 맛 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadStoreFlavorMapper, HeadStoreMapper, HeadFlavorMapper -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreFlavorService {

    private final HeadStoreFlavorMapper headStoreFlavorMapper;
    private final HeadStoreMapper headStoreMapper;
    private final HeadFlavorMapper headFlavorMapper;

    // 지점별 맛 배정
    @Transactional
    /**
     * [메서드 흐름] addStoreFlavor
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreFlavorMapper, HeadStoreMapper, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] getStoreFlavorList
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreFlavorMapper, HeadStoreMapper, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadStoreFlavorListResponseDTO> getStoreFlavorList(Integer storeId) {

        headStoreMapper.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점입니다."));

        return headStoreFlavorMapper.findByStore_IdOrderByIdDesc(storeId)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 지점별 맛 상세 조회
    /**
     * [메서드 흐름] getStoreFlavorDetail
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreFlavorMapper, HeadStoreMapper, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadStoreFlavorDetailResponseDTO getStoreFlavorDetail(
            Integer storeId,
            Integer storeFlavorId) {

        StoreFlavor storeFlavor = headStoreFlavorMapper.findByStore_IdAndId(storeId, storeFlavorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 맛 정보입니다."));

        return toDetailResponseDTO(storeFlavor);
    }

    // 지점별 맛 품절 여부 / 통 개수 수정
    @Transactional
    /**
     * [메서드 흐름] updateStoreFlavor
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreFlavorMapper, HeadStoreMapper, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] deleteStoreFlavor
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreFlavorMapper, HeadStoreMapper, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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