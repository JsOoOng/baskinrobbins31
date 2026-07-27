package com.kiosk.branch.kiosk.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import com.kiosk.branch.kiosk.dto.BranchKioskResponse;
import com.kiosk.branch.kiosk.dto.KioskCreateRequest;
import com.kiosk.branch.kiosk.dto.KioskStatusRequest;
import com.kiosk.branch.kiosk.service.BranchKioskService;

import lombok.RequiredArgsConstructor;



/**
 * [코드 흐름 안내] BranchKioskController
 *
 * <p>역할: 지점 운영의 키오스크 기기 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/kiosk) -> BranchKioskService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/branch/kiosk")
public class BranchKioskController {


    private final BranchKioskService kioskService;



    /*
     * 지점 키오스크 조회
     */
    /**
     * [요청 흐름] GET /branch/kiosk/{storeId}
     * 프론트 요청을 받아 getKiosks() 메서드가 입력을 받고 BranchKioskService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<List<BranchKioskResponse>> getKiosks(
            @PathVariable Integer storeId
    ){

        return ResponseEntity.ok(
                kioskService.getKiosks(storeId)
        );

    }

    /*
     * 전체 키오스크 조회 (전체 지점)
     */
    /**
     * [요청 흐름] GET /branch/kiosk/all
     * 프론트 요청을 받아 getAllKiosks() 메서드가 입력을 받고 BranchKioskService 호출 후 결과를 응답한다.
     */
    @GetMapping("/all")
    public ResponseEntity<List<BranchKioskResponse>> getAllKiosks(){
        return ResponseEntity.ok(
                kioskService.getAllKiosks()
        );
    }



    /*
     * 키오스크 상태 변경
     */
    /**
     * [요청 흐름] PATCH /branch/kiosk/{kioskId}/status
     * 프론트 요청을 받아 updateStatus() 메서드가 입력을 받고 BranchKioskService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/{kioskId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Integer kioskId,
            @RequestBody KioskStatusRequest request
    ){

        kioskService.updateStatus(
                kioskId,
                request
        );


        return ResponseEntity.ok().build();

    }
    
    /**
     * [요청 흐름] POST /branch/kiosk
     * 프론트 요청을 받아 createKiosk() 메서드가 입력을 받고 BranchKioskService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<Void> createKiosk(
            @RequestBody KioskCreateRequest request
    ){

        kioskService.createKiosk(request);


        return ResponseEntity.ok().build();

    }

    /**
     * [요청 흐름] GET /branch/kiosk/stream/{storeId}
     * 프론트 요청을 받아 streamKiosks() 메서드가 입력을 받고 BranchKioskService 호출 후 결과를 응답한다.
     */
    @GetMapping(value = "/stream/{storeId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamKiosks(@PathVariable Integer storeId) {
        return kioskService.subscribe(storeId);
    }

}