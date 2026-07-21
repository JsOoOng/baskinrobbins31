package com.kiosk.customer.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 전화번호에서 하이픈('-')을 제거한 값끼리 비교해서 찾기
    @Query("SELECT u FROM User u WHERE REPLACE(u.phone, '-', '') = REPLACE(:phone, '-', '')")
    Optional<User> findByPhoneIgnoringHyphen(@Param("phone") String phone);
}