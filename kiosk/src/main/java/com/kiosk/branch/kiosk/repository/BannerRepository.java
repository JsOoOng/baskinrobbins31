package com.kiosk.branch.kiosk.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kiosk.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [코드 흐름 안내] BannerRepository
 *
 * <p>역할: 지점 운영의 배너 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(BANNERS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface BannerRepository extends JpaRepository<Banner, Integer> {

    /*
     * 쉬운주석: 활성화되어 있고 현재 시간이 노출 기간 안에 있는 배너만 고객 화면에 전달한다.
     * 날짜가 없는 기존 배너는 기간 제한 없이 그대로 노출된다.
     */
    @Query("""
            select b from Banner b
            where b.isActive = true
              and (b.startDate is null or b.startDate <= :now)
              and (b.endDate is null or b.endDate > :now)
            order by b.id desc
            """)
    List<Banner> findVisibleAt(@Param("now") LocalDateTime now);
}
