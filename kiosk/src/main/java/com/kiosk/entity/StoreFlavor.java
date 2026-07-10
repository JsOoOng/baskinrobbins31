package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORE_FLAVORS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreFlavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_flavor_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flavor_id", nullable = false)
    private IcecreamFlavor flavor;

    @Column(name = "container")
    @Builder.Default
    private Integer container = 0;

    @Column(name = "is_sold_out")
    @Builder.Default
    private Boolean isSoldOut = false;

    public void changeSoldOut(Boolean isSoldOut) {
        this.isSoldOut = isSoldOut;
    }

    public void updateStoreFlavor(Boolean isSoldOut, Integer container) {
        this.isSoldOut = isSoldOut;
        this.container = container;
    }
    
    public void changeContainer(Integer amount) {

        this.container += amount;


        if(this.container < 0){

            this.container = 0;

        }

    }
}