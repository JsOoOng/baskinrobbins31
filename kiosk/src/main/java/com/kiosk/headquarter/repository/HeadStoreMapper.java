package com.kiosk.headquarter.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Store;

/**
 * [코드 흐름 안내] HeadStoreMapper
 *
 * <p>역할: 본사 관리의 지점 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(STORES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadStoreMapper
    extends JpaRepository<Store, Integer> {

    long countByStoreStatus(com.kiosk.entity.enums.StoreStatus status);
    long countByCreatedAtLessThanEqual(LocalDateTime createdAt);


    /*
     * 지점 번호 내림차순 목록
     */
    List<Store>
            findAllByOrderByIdDesc();

    /*
     * 사업자 번호 중복 확인
     */
    boolean existsByBusinessNumber(
            String businessNumber
    );
}
