package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] SystemSetting
 *
 * <p>역할: 환경 설정 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 SYSTEM_SETTINGS 테이블 매핑을 통해 이 객체를 저장·조회한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "SYSTEM_SETTINGS")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SystemSetting {

    @Id
    @Column(name = "id")
    @Builder.Default
    private Integer id = 1;

    // --- 일반 설정 ---
    @Column(name = "use_voice_guide")
    @Builder.Default
    private Boolean useVoiceGuide = true;

    @Column(name = "receipt_print_mode", length = 20)
    @Builder.Default
    private String receiptPrintMode = "SELECT"; // ALWAYS, NEVER, SELECT

    // --- 주문/결제 설정 ---
    @Column(name = "tumbler_discount_amount")
    @Builder.Default
    private Integer tumblerDiscountAmount = 300;

    @Column(name = "low_stock_alert_count")
    @Builder.Default
    private Integer lowStockAlertCount = 5;

    @Column(name = "use_easy_pay")
    @Builder.Default
    private Boolean useEasyPay = true; // 카카오페이, 네이버페이 등

    // --- 타임스탬프 ---
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
