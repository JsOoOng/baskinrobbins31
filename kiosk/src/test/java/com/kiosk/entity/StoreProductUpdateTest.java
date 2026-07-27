package com.kiosk.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StoreProductUpdateTest {

    /* 쉬운주석: 수정 화면에서 보낸 가격이 지점 상품 객체에 실제로 저장되는지 확인한다. */
    @Test
    void changesStoreProductPrice() {
        StoreProduct storeProduct = StoreProduct.builder()
                .storeProductPrice(3500)
                .build();

        storeProduct.changeStoreProductPrice(4200);

        assertEquals(4200, storeProduct.getStoreProductPrice());
    }
}
