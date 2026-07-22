package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Store;
import com.kiosk.entity.enums.StoreStatus;
import com.kiosk.headquarter.dto.store.HeadStoreCreateRequest;
import com.kiosk.headquarter.dto.store.HeadStoreResponse;
import com.kiosk.headquarter.dto.store.HeadStoreUpdateRequest;
import com.kiosk.headquarter.repository.HeadStoreMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreService {

    private final HeadStoreMapper
            headStoreMapper;

    private final AdminLogService
            adminLogService;

    /*
     * 전체 지점 목록 조회
     */
    public List<HeadStoreResponse> getStores() {

        return headStoreMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(HeadStoreResponse::from)
                .toList();
    }

    /*
     * 지점 상세 조회
     */
    public HeadStoreResponse getStore(
            Integer storeId
    ) {

        Store store =
                findStore(storeId);

        return HeadStoreResponse.from(store);
    }

    /*
     * 신규 지점 등록
     */
    @Transactional
    public HeadStoreResponse createStore(
            HeadStoreCreateRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "지점 등록 요청이 없습니다."
            );
        }

        validateStoreRequest(
                request.getStoreName(),
                request.getRegion(),
                request.getAddress(),
                request.getStoreStatus()
        );

        validateBusinessNumber(
                null,
                request.getBusinessNumber()
        );

        Store store =
                Store.builder()
                        .storeName(
                                request.getStoreName()
                                        .trim()
                        )
                        .businessNumber(
                                normalizeNullable(
                                        request.getBusinessNumber()
                                )
                        )
                        .region(
                                request.getRegion()
                                        .trim()
                        )
                        .address(
                                request.getAddress()
                                        .trim()
                        )
                        .phone(
                                normalizeNullable(
                                        request.getPhone()
                                )
                        )
                        .businessHours(
                                normalizeNullable(
                                        request.getBusinessHours()
                                )
                        )
                        .storeStatus(
                                request.getStoreStatus()
                        )
                        .build();

        Store savedStore =
                headStoreMapper.save(store);

        adminLogService.logAction("지점", savedStore.getStoreName() + " 신규 등록");

        return HeadStoreResponse.from(
                savedStore
        );
    }

    /*
     * 지점 정보 수정
     */
    @Transactional
    public HeadStoreResponse updateStore(
            Integer storeId,
            HeadStoreUpdateRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "지점 수정 요청이 없습니다."
            );
        }

        validateStoreRequest(
                request.getStoreName(),
                request.getRegion(),
                request.getAddress(),
                request.getStoreStatus()
        );

        Store store =
                findStore(storeId);

        validateBusinessNumber(
                store.getBusinessNumber(),
                request.getBusinessNumber()
        );

        store.updateStore(
                request.getStoreName()
                        .trim(),

                normalizeNullable(
                        request.getBusinessNumber()
                ),

                request.getRegion()
                        .trim(),

                request.getAddress()
                        .trim(),

                normalizeNullable(
                        request.getPhone()
                ),

                normalizeNullable(
                        request.getBusinessHours()
                ),

                request.getStoreStatus()
        );

        /*
         * @Transactional 안에서 조회한 엔티티이므로
         * JPA 변경 감지로 UPDATE가 실행됩니다.
         */
        adminLogService.logAction("지점", store.getStoreName() + " 정보 수정");

        return HeadStoreResponse.from(store);
    }

    /*
     * 지점 조회 공통 처리
     */
    private Store findStore(
            Integer storeId
    ) {

        if (storeId == null) {
            throw new IllegalArgumentException(
                    "지점 번호가 없습니다."
            );
        }

        return headStoreMapper
                .findById(storeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 지점입니다."
                        )
                );
    }

    /*
     * 필수 지점 정보 검증
     */
    private void validateStoreRequest(
            String storeName,
            String region,
            String address,
            StoreStatus storeStatus
    ) {

        if (
                storeName == null ||
                storeName.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "지점명을 입력해주세요."
            );
        }

        if (
                region == null ||
                region.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "지역을 입력해주세요."
            );
        }

        if (
                address == null ||
                address.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "주소를 입력해주세요."
            );
        }

        if (storeStatus == null) {
            throw new IllegalArgumentException(
                    "지점 상태를 선택해주세요."
            );
        }
    }

    /*
     * 사업자 번호 중복 확인
     */
    private void validateBusinessNumber(
            String currentBusinessNumber,
            String requestedBusinessNumber
    ) {

        String normalized =
                normalizeNullable(
                        requestedBusinessNumber
                );

        /*
         * 사업자 번호는 선택 입력
         */
        if (normalized == null) {
            return;
        }

        /*
         * 수정 전과 동일한 번호라면
         * 중복 검사를 하지 않습니다.
         */
        if (
                normalized.equals(
                        currentBusinessNumber
                )
        ) {
            return;
        }

        if (
                headStoreMapper
                        .existsByBusinessNumber(
                                normalized
                        )
        ) {
            throw new IllegalArgumentException(
                    "이미 등록된 사업자 번호입니다."
            );
        }
    }

    /*
     * 빈 문자열을 null로 정리
     */
    private String normalizeNullable(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}