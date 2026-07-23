package com.kiosk.customer.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

/**
 * [코드 흐름 안내] IcecreamFlavorRepository
 *
 * <p>역할: 고객 키오스크의 아이스크림 맛 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(icecream_flavors) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface IcecreamFlavorRepository extends JpaRepository<IcecreamFlavor, Integer> {
    // 현재 판매 운영 중(isActive = true)인 맛 리스트만 조회
    List<IcecreamFlavor> findByIsActiveTrue();
}