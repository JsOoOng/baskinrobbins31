package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.InquiryStatus;
import com.kiosk.entity.enums.InquiryType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "INQUIRIES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type", nullable = false)
    private InquiryType inquiryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private HeadquarterAdmin admin;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private InquiryStatus status = InquiryStatus.WAITING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void writeAnswer(String answer, HeadquarterAdmin admin) {
        this.answer = answer;
        this.admin = admin;
        this.status = InquiryStatus.ANSWERED;
    }
}