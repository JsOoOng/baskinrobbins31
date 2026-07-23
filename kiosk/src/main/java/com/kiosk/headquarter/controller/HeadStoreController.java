package com.kiosk.headquarter.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
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

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadStoreController
 *
 * <p>역할: 본사 관리의 지점 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/stores) -> HeadStoreService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    /**
     * [요청 흐름] GET /head/stores
     * 프론트 요청을 받아 getStores() 메서드가 입력을 받고 HeadStoreService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] GET /head/stores/{storeId}
     * 프론트 요청을 받아 getStore() 메서드가 입력을 받고 HeadStoreService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] POST /head/stores
     * 프론트 요청을 받아 createStore() 메서드가 입력을 받고 HeadStoreService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] PUT /head/stores/{storeId}
     * 프론트 요청을 받아 updateStore() 메서드가 입력을 받고 HeadStoreService 호출 후 결과를 응답한다.
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