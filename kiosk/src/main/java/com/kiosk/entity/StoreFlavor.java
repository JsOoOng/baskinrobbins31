package com.kiosk.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UpdateTimestamp;

import com.kiosk.entity.enums.AutoRestockMode;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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



    /*
     * 현재 재고(Container)
     */
    @Column(name = "container", nullable = false)
    @Builder.Default
    private Integer container = 0;



    /*
     * 품절 여부
     */
    @Column(name = "is_sold_out", nullable = false)
    @Builder.Default
    private Boolean isSoldOut = false;



    /*
     * 최소 재고
     */
    @Column(name = "min_stock", nullable = false)
    @Builder.Default
    private Integer minStock = 1;



    /*
     * 목표 재고
     */
    @Column(name = "target_stock", nullable = false)
    @Builder.Default
    private Integer targetStock = 10;



    /*
     * 자동 보충 여부
     */
    @Column(name = "auto_restock_enabled", nullable = false)
    @Builder.Default
    private Boolean autoRestockEnabled = true;



    /*
     * 자동 보충 방식
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "restock_mode", nullable = false, length = 20)
    @Builder.Default
    private AutoRestockMode restockMode =
            AutoRestockMode.THRESHOLD;



    /*
     * 마지막 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @OneToMany(
            mappedBy = "storeFlavor",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
        )
        @Builder.Default
        private List<RestockRequest> restockRequests =
                new ArrayList<>();



    @PrePersist
    protected void initialize() {

        if (container == null) {
            container = 0;
        }

        if (isSoldOut == null) {
            isSoldOut = false;
        }

        if (minStock == null) {
            minStock = 1;
        }

        if (targetStock == null) {
            targetStock = 10;
        }

        if (autoRestockEnabled == null) {
            autoRestockEnabled = true;
        }

        if (restockMode == null) {
            restockMode = AutoRestockMode.THRESHOLD;
        }
    }



    /*
     * 품절 변경
     */
    public void changeSoldOut(Boolean isSoldOut) {

        this.isSoldOut = isSoldOut;
    }



    /*
     * 지점 맛 재고 수정
     */
    public void updateStoreFlavor(
            Boolean isSoldOut,
            Integer container
    ) {

        this.isSoldOut = isSoldOut;
        this.container = container;
    }



    /*
     * 재고 증가
     *
     * 본사 발주 완료 입고 처리용
     */
    public void increaseStock(Integer amount) {

        if (
            amount == null ||
            amount <= 0
        ) {
            throw new IllegalArgumentException(
                    "입고 수량은 1 이상이어야 합니다."
            );
        }


        if (container == null) {
            container = 0;
        }


        this.container += amount;


        // 재고가 들어오면 품절 해제
        this.isSoldOut = false;
    }



    /*
     * 재고 변경
     *
     * 기존 사용 유지
     */
    public void changeContainer(Integer amount) {

        if (container == null) {
            container = 0;
        }


        this.container += amount;


        if (this.container < 0) {
            this.container = 0;
        }


        this.isSoldOut =
                (this.container <= 0);
    }



    /*
     * 자동 보충 설정 변경
     */
    public void updateAutoRestockSetting(
            Integer minStock,
            Integer targetStock,
            Boolean autoRestockEnabled,
            AutoRestockMode restockMode
    ) {

        if(minStock == null || minStock < 0){

            throw new IllegalArgumentException(
                    "최소 재고는 0 이상이어야 합니다."
            );
        }


        if(targetStock == null || targetStock <= 0){

            throw new IllegalArgumentException(
                    "목표 재고는 1 이상이어야 합니다."
            );
        }


        if(targetStock < minStock){

            throw new IllegalArgumentException(
                    "목표 재고는 최소 재고보다 작을 수 없습니다."
            );
        }


        if(restockMode == null){

            throw new IllegalArgumentException(
                    "자동 보충 방식을 선택해주세요."
            );
        }


        this.minStock = minStock;

        this.targetStock = targetStock;

        this.autoRestockEnabled =
                Boolean.TRUE.equals(autoRestockEnabled);

        this.restockMode = restockMode;
    }



    /*
     * 자동 보충 필요 수량 계산
     */
    public Integer calculateRestockQuantity(){

        if(
            container == null ||
            targetStock == null ||
            targetStock <= container
        ){
            return 0;
        }


        return targetStock - container;
    }



    /*
     * 자동 보충
     */
    public Integer autoRestock(){

        Integer quantity =
                calculateRestockQuantity();


        if(quantity <= 0){
            return 0;
        }


        this.container += quantity;

        this.isSoldOut = false;


        return quantity;
    }



    /*
     * 임계 재고 보충 대상 확인
     */
    public boolean needsThresholdRestock() {

        if(
            !Boolean.TRUE.equals(autoRestockEnabled)
        ){
            return false;
        }


        if(
            restockMode != AutoRestockMode.THRESHOLD &&
            restockMode != AutoRestockMode.BOTH
        ){
            return false;
        }


        if(
            container == null ||
            minStock == null
        ){
            return false;
        }


        return container <= minStock;
    }



    /*
     * 정기 보충 대상 확인
     */
    public boolean needsDailyRestock(){

        if(!Boolean.TRUE.equals(autoRestockEnabled)){
            return false;
        }


        if(
            restockMode != AutoRestockMode.DAILY &&
            restockMode != AutoRestockMode.BOTH
        ){
            return false;
        }


        return container < targetStock;
    }

}
