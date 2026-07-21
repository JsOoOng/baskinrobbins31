package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.headquarter.dto.restock.HeadRestockDetailResponseDTO;
import com.kiosk.headquarter.dto.restock.HeadRestockListResponseDTO;
import com.kiosk.headquarter.dto.restock.HeadRestockProcessRequestDTO;
import com.kiosk.headquarter.repository.HeadRestockRequestMapper;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;
import com.kiosk.headquarter.repository.HeadquarterAdminMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadRestockService {

    private final HeadRestockRequestMapper headRestockRequestMapper;
    private final HeadquarterAdminMapper headquarterAdminMapper;
    private final HeadStoreInventoryMapper headStoreInventoryMapper;

    // 발주 요청 전체 목록 조회
    public List<HeadRestockListResponseDTO> getRestockList() {
        return headRestockRequestMapper.findAllByOrderByIdDesc()
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 승인 대기 발주 요청 목록 조회
    public List<HeadRestockListResponseDTO> getWaitingRestockList() {
        return headRestockRequestMapper.findByStatusOrderByIdDesc(RestockStatus.WAITING)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 발주 요청 상세 조회
    public HeadRestockDetailResponseDTO getRestockDetail(Integer requestId) {
        RestockRequest restockRequest = headRestockRequestMapper.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 요청입니다."));

        return toDetailResponseDTO(restockRequest);
    }

    // 발주 승인
    @Transactional
    public String approveRestock(Integer requestId, HeadRestockProcessRequestDTO requestDTO) {
        RestockRequest restockRequest = headRestockRequestMapper.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 요청입니다."));

        HeadquarterAdmin admin = getActiveAdmin(requestDTO.getAdminId());

        restockRequest.approve(admin);

        return "발주 요청 승인 성공";
    }

    // 배송 처리
    @Transactional
    public String startShipping(Integer requestId, HeadRestockProcessRequestDTO requestDTO) {
        RestockRequest restockRequest = headRestockRequestMapper.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 요청입니다."));

        HeadquarterAdmin admin = getActiveAdmin(requestDTO.getAdminId());

        restockRequest.startShipping(admin);

        return "발주 요청 배송 처리 성공";
    }

    // 완료 처리
    @Transactional
    public String completeRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {

        RestockRequest restockRequest =
                headRestockRequestMapper
                        .findById(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 발주 요청입니다."
                                )
                        );

        HeadquarterAdmin admin =
                getActiveAdmin(
                        requestDTO.getAdminId()
                );

        /*
         * SHIPPING 상태일 때만 COMPLETED로 변경됩니다.
         */
        restockRequest.complete(admin);

        StoreInventory inventory =
                headStoreInventoryMapper
                        .findByStoreAndItem(
                                restockRequest
                                        .getStore(),
                                restockRequest
                                        .getItem()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 지점의 재고 정보가 없습니다."
                                )
                        );

        inventory.increaseStock(
                restockRequest
                        .getRequestQuantity()
        );

        return "발주 완료 및 재고 입고 처리 성공";
    }

    private HeadquarterAdmin getActiveAdmin(Integer adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("처리 관리자 adminId가 필요합니다.");
        }

        HeadquarterAdmin admin = headquarterAdminMapper.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));

        if (!admin.isActiveAdmin()) {
            throw new IllegalArgumentException("비활성화된 본사 관리자는 발주 요청을 처리할 수 없습니다.");
        }

        return admin;
    }

    private HeadRestockListResponseDTO toListResponseDTO(RestockRequest restockRequest) {
        Integer adminId = null;
        String adminName = null;

        if (restockRequest.getAdmin() != null) {
            adminId = restockRequest.getAdmin().getId();
            adminName = restockRequest.getAdmin().getName();
        }

        return HeadRestockListResponseDTO.builder()
                .requestId(restockRequest.getId())
                .storeId(restockRequest.getStore().getId())
                .storeName(restockRequest.getStore().getStoreName())
                .itemId(restockRequest.getItem().getId())
                .itemName(restockRequest.getItem().getProduct().getProductName())
                .unit(restockRequest.getItem().getUnit())
                .requestQuantity(restockRequest.getRequestQuantity())
                .status(restockRequest.getStatus())
                .adminId(adminId)
                .adminName(adminName)
                .requestedAt(restockRequest.getRequestedAt())
                .build();
    }

    private HeadRestockDetailResponseDTO toDetailResponseDTO(RestockRequest restockRequest) {
        Integer adminId = null;
        String adminName = null;

        if (restockRequest.getAdmin() != null) {
            adminId = restockRequest.getAdmin().getId();
            adminName = restockRequest.getAdmin().getName();
        }

        Integer unitPrice = restockRequest.getItem().getUnitPrice();
        Integer requestQuantity = restockRequest.getRequestQuantity();

        return HeadRestockDetailResponseDTO.builder()
                .requestId(restockRequest.getId())
                .storeId(restockRequest.getStore().getId())
                .storeName(restockRequest.getStore().getStoreName())
                .itemId(restockRequest.getItem().getId())
                .itemName(restockRequest.getItem().getProduct().getProductName())
                .unit(restockRequest.getItem().getUnit())
                .unitPrice(unitPrice)
                .requestQuantity(requestQuantity)
                .totalPrice(unitPrice * requestQuantity)
                .status(restockRequest.getStatus())
                .adminId(adminId)
                .adminName(adminName)
                .requestedAt(restockRequest.getRequestedAt())
                .build();
    }
    
    
}