package com.kiosk.customer.flavor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

/**
 * [코드 흐름 안내] FlavorRepository
 *
 * <p>역할: 고객 키오스크의 아이스크림 맛 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(icecream_flavors) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface FlavorRepository extends JpaRepository<IcecreamFlavor, Integer> {

    // 특정 지점에서 판매 가능한 맛 조회 (품절 포함)
    @Query("""
        SELECT sf
        FROM StoreFlavor sf
        WHERE sf.store.id = :storeId
          AND sf.flavor.isActive = true
    """)
    List<com.kiosk.entity.StoreFlavor> findAvailableFlavorsByStoreId(
            @Param("storeId") Long storeId
    );
}