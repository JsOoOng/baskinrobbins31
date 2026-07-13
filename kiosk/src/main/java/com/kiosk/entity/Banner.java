package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BANNERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Integer id;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public void updateBanner(String title, String imageUrl, Boolean isActive) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
    }

    public void deactivate() {
        this.isActive = false;
    }
}