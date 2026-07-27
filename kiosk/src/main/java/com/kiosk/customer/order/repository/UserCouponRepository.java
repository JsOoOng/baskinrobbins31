package com.kiosk.customer.order.repository;

import com.kiosk.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * [코드 흐름 안내] UserCouponRepository
 *
 * <p>역할: 고객 키오스크의 쿠폰 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(USER_COUPON) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Integer> {

    /*
     * 쉬운주석: 고객 화면에는 아직 사용하지 않았고 오늘까지 유효한 쿠폰만 보여준다.
     * 만료된 쿠폰은 DB 기록은 남지만 노출 목록에서는 자동으로 사라진다.
     */
    @Query("""
            SELECT uc FROM UserCoupon uc
            WHERE uc.user.id = :userId
              AND uc.isUsed = false
              AND uc.expiryDate >= CURRENT_DATE
            """)
    List<UserCoupon> findByUserId(@Param("userId") Integer userId);

}
