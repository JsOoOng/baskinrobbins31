package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Store;
import com.kiosk.headquarter.dto.store.HeadStoreCreateRequest;
import com.kiosk.headquarter.dto.store.HeadStoreResponse;
import com.kiosk.headquarter.dto.store.HeadStoreUpdateRequest;
import com.kiosk.headquarter.repository.HeadStoreMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreService {

    private final HeadStoreMapper headStoreMapper;

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

        Store store =
                findStore(storeId);

        validateBusinessNumber(
                store.getBusinessNumber(),
                request.getBusinessNumber()
        );

        store.updateStore(
                request.getStoreName().trim(),

                normalizeNullable(
                        request.getBusinessNumber()
                ),

                request.getRegion().trim(),

                request.getAddress().trim(),

                normalizeNullable(
                        request.getPhone()
                ),

                normalizeNullable(
                        request.getBusinessHours()
                ),

                request.getStoreStatus()
        );

        /*
         * JPA 변경 감지로 UPDATE가 실행됩니다.
         */
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
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "존재하지 않는 지점입니다."
                                )
                );
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
         * 수정 전과 동일한 번호라면 검사하지 않음
         */
        if (normalized.equals(
                currentBusinessNumber
        )) {
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