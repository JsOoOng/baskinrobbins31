package com.kiosk.customer.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.User;

/**
 * [코드 흐름 안내] UserRepository
 *
 * <p>역할: 고객 키오스크의 회원 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(USERS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 전화번호에서 하이픈('-')을 제거한 값끼리 비교해서 찾기
    @Query("SELECT u FROM User u WHERE REPLACE(u.phone, '-', '') = REPLACE(:phone, '-', '')")
    Optional<User> findByPhoneIgnoringHyphen(@Param("phone") String phone);
}