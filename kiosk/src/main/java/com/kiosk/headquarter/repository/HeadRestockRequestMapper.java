package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.enums.RestockStatus;

/**
 * [코드 흐름 안내] HeadRestockRequestMapper
 *
 * <p>역할: 본사 관리의 재입고·발주 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(RESTOCK_REQUESTS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadRestockRequestMapper extends JpaRepository<RestockRequest, Integer> {

    long countByStatus(RestockStatus status);

    List<RestockRequest> findAllByOrderByIdDesc();

    List<RestockRequest> findByStatusOrderByIdDesc(RestockStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM RestockRequest r LEFT JOIN r.storeInventory si LEFT JOIN r.storeFlavor sf WHERE si.store.id = :storeId OR sf.store.id = :storeId ORDER BY r.id DESC")
    List<RestockRequest> findByStoreIdOrderByIdDesc(@org.springframework.data.repository.query.Param("storeId") Integer storeId);
}