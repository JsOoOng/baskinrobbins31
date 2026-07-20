package com.kiosk.headquarter.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.store.HeadStoreCreateRequest;
import com.kiosk.headquarter.dto.store.HeadStoreResponse;
import com.kiosk.headquarter.dto.store.HeadStoreUpdateRequest;
import com.kiosk.headquarter.service.HeadStoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/stores")
@RequiredArgsConstructor
public class HeadStoreController {

    private final HeadStoreService headStoreService;

    /*
     * 전체 지점 목록 조회
     *
     * GET /head/stores
     */
    @GetMapping
    public ResponseEntity<List<HeadStoreResponse>>
            getStores() {

        List<HeadStoreResponse> stores =
                headStoreService.getStores();

        return ResponseEntity.ok(stores);
    }

    /*
     * 지점 상세 조회
     *
     * GET /head/stores/{storeId}
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<HeadStoreResponse>
            getStore(
                    @PathVariable
                    Integer storeId
            ) {

        HeadStoreResponse store =
                headStoreService.getStore(
                        storeId
                );

        return ResponseEntity.ok(store);
    }

    /*
     * 신규 지점 등록
     *
     * POST /head/stores
     */
    @PostMapping
    public ResponseEntity<HeadStoreResponse>
            createStore(
                    @Valid
                    @RequestBody
                    HeadStoreCreateRequest request
            ) {

        HeadStoreResponse createdStore =
                headStoreService.createStore(
                        request
                );

        URI location =
                URI.create(
                        "/head/stores/"
                                + createdStore
                                        .getStoreId()
                );

        return ResponseEntity
                .created(location)
                .body(createdStore);
    }

    /*
     * 지점 정보 수정
     *
     * PUT /head/stores/{storeId}
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<HeadStoreResponse>
            updateStore(
                    @PathVariable
                    Integer storeId,

                    @Valid
                    @RequestBody
                    HeadStoreUpdateRequest request
            ) {

        HeadStoreResponse updatedStore =
                headStoreService.updateStore(
                        storeId,
                        request
                );

        return ResponseEntity.ok(
                updatedStore
        );
    }
}