package com.kiosk.entity;

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
     * 배너 기본 정보 수정
     */
    public void updateBanner(
            String title,
            String imageUrl,
            Boolean isActive
    ) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
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
}