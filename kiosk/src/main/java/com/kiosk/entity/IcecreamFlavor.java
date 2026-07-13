package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ICECREAM_FLAVORS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IcecreamFlavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flavor_id")
    private Integer id;

    @Column(name = "flavor_name", length = 50, nullable = false)
    private String flavorName;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    public void updateFlavor(String flavorName, Boolean isActive, String imageUrl) {
        this.flavorName = flavorName;
        this.isActive = isActive;
        this.imageUrl = imageUrl;
    }

    public void deactivate() {
        this.isActive = false;
    }
}