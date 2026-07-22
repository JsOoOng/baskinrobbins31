package com.kiosk.customer.order.repository;

import com.kiosk.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Integer> {

    // 엔티티 필드명이 uc.user.id 인 경우
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.user.id = :userId")
    List<UserCoupon> findByUserId(@Param("userId") Integer userId);

}