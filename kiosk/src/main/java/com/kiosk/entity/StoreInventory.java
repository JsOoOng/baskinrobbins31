package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "STORE_INVENTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_inventory_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Column(name = "current_stock")
    @Builder.Default
    private Integer currentStock = 0;

    @UpdateTimestamp // 업데이트 될 때마다 시간이 자동으로 갱신됩니다.
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    public void decreaseStock(Integer amount){

        if(currentStock < amount){
            throw new IllegalArgumentException(
                "재고 부족"
            );
        }

        this.currentStock -= amount;
    }
}