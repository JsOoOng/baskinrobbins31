package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] KioskBanner
 *
 * <p>역할: 키오스크 배너 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 kiosk_banner 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 kiosk_id, banner_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "kiosk_banner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KioskBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kb_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kiosk_id", nullable = false)
    private Kiosk kiosk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banner_id", nullable = false)
    private Banner banner;

    public void updateBanner(Banner banner) {
        this.banner = banner;
    }
}
