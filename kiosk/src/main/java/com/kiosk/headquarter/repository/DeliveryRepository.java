package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kiosk.entity.Delivery;

/**
 * [코드 흐름 안내] DeliveryRepository
 *
 * <p>역할: 본사 관리의 배송 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(DELIVERIES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface DeliveryRepository
        extends JpaRepository<Delivery, Integer> {


    boolean existsByRestockRequestId(
            Integer requestId
    );

    Optional<Delivery> findByRestockRequestId(Integer requestId);



    @Query("""
            select d
            from Delivery d

            join fetch d.restockRequest r

            left join fetch r.storeInventory si
            left join fetch si.item i
            left join fetch si.store s

            left join fetch r.storeFlavor sf
            left join fetch sf.flavor f

            order by d.id desc
    """)
    List<Delivery> findAllDelivery();

}
