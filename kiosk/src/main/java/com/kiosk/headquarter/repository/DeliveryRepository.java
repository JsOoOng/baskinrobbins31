package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kiosk.entity.Delivery;
import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.enums.RestockStatus;

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

    /*
     * 승인됐지만 과거 로직 때문에 배송 행이 만들어지지 않은 신청을 찾습니다.
     * 배송 목록 조회 시 누락 데이터를 READY 배송으로 자동 복구하는 데 사용합니다.
     */
    @Query("""
            select r
            from RestockRequest r
            where r.status = :status
              and not exists (
                select d.id
                from Delivery d
                where d.restockRequest = r
              )
            """)
    List<RestockRequest> findRequestsWithoutDelivery(
            @Param("status") RestockStatus status
    );



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
