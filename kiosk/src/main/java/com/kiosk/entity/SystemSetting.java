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
    @Column(name = "screensaver_time")
    @Builder.Default
    private Integer screensaverTime = 60; // 초 단위

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
