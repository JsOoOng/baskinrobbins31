package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;

/**
 * [코드 흐름 안내] StoreFlavorRepository
 *
 * <p>역할: 본사 관리의 지점별 맛 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(STORE_FLAVORS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface StoreFlavorRepository
        extends JpaRepository<StoreFlavor, Integer> {


    /*
     * 전체 지점 맛 재고 조회
     */
    List<StoreFlavor> findAll();


    /*
     * 특정 지점 맛 재고 조회
     */
    List<StoreFlavor> findByStore_Id(
            Integer storeId
    );


    /*
     * 자동 보충 대상 조회용
     *
     * 추후 Scheduler에서 사용
     */
    List<StoreFlavor> findByAutoRestockEnabledTrue();
    
    List<StoreFlavor>
    findByAutoRestockEnabledTrueAndRestockModeIn(
            List<AutoRestockMode> modes
    );
}