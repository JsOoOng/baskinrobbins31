package com.kiosk.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] Banner
 *
 * <p>역할: 배너 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 BANNERS 테이블 매핑을 통해 이 객체를 저장·조회한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "BANNERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "banner_id")
    private Integer id;

    @Column(
            name = "title",
            length = 100
    )
    private String title;

    @Column(
            name = "image_url",
            length = 255,
            nullable = false
    )
    private String imageUrl;

    @Builder.Default
    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive = true;

    /*
     * 쉬운주석: 이 두 날짜 사이에서만 배너를 노출할 수 있다.
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    /*
     * 배너 기본 정보 수정
     */
    public void updateBanner(
            String title,
            String imageUrl,
            Boolean isActive,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /*
     * 배너 노출 상태 변경
     */
    public void changeActive(
            Boolean isActive
    ) {
        this.isActive =
                Boolean.TRUE.equals(isActive);
    }

    /*
     * 쉬운주석: 지정한 시각이 노출 기간 안이고 활성 상태인지 한 번에 확인한다.
     */
    public boolean isVisibleAt(LocalDateTime dateTime) {
        return Boolean.TRUE.equals(isActive)
                && (startDate == null || !startDate.isAfter(dateTime))
                && (endDate == null || endDate.isAfter(dateTime));
    }
}
