package com.kiosk.headquarter.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Store;
import com.kiosk.entity.StoreStatusHistory;
import com.kiosk.entity.enums.StoreStatus;
import com.kiosk.headquarter.dto.store.HeadStoreCreateRequest;
import com.kiosk.headquarter.dto.store.HeadStoreResponse;
import com.kiosk.headquarter.dto.store.HeadStoreUpdateRequest;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.repository.StoreStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadStoreService
 *
 * <p>역할: 본사 관리의 지점 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadStoreMapper, AdminLogService -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreService {

    private final HeadStoreMapper
            headStoreMapper;

    private final AdminLogService
            adminLogService;
    private final StoreStatusHistoryRepository storeStatusHistoryRepository;

    /*
     * 전체 지점 목록 조회
     */
    /**
     * [메서드 흐름] getStores
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] getStore
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] createStore
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

        saveStatusHistory(
                savedStore,
                savedStore.getStoreStatus(),
                LocalDateTime.now()
        );
        adminLogService.logAction("지점", savedStore.getStoreName() + " 신규 등록");

        return HeadStoreResponse.from(
                savedStore
        );
    }

    /*
     * 지점 정보 수정
     */
    @Transactional
    /**
     * [메서드 흐름] updateStore
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
        StoreStatus previousStatus = store.getStoreStatus();

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

        if (previousStatus != request.getStoreStatus()) {
            /*
             * 기존 지점은 이력 테이블 도입 전에 만들어졌을 수 있습니다.
             * 첫 변경 때 생성 시점의 이전 상태를 함께 기록해야 과거 상태를 복원할 수 있습니다.
             */
            if (!storeStatusHistoryRepository.existsByStore_Id(storeId)) {
                saveStatusHistory(
                        store,
                        previousStatus,
                        store.getCreatedAt() != null
                                ? store.getCreatedAt()
                                : LocalDateTime.now().minusNanos(1)
                );
            }
            saveStatusHistory(
                    store,
                    request.getStoreStatus(),
                    LocalDateTime.now()
            );
        }

        /*
         * @Transactional 안에서 조회한 엔티티이므로
         * JPA 변경 감지로 UPDATE가 실행됩니다.
         */
        adminLogService.logAction("지점", store.getStoreName() + " 정보 수정");

        return HeadStoreResponse.from(store);
    }

    private void saveStatusHistory(
            Store store,
            StoreStatus status,
            LocalDateTime changedAt
    ) {
        storeStatusHistoryRepository.save(
                StoreStatusHistory.builder()
                        .store(store)
                        .storeStatus(status)
                        .changedAt(changedAt)
                        .build()
        );
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
