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

import com.kiosk.branch.kiosk.dto.BranchKioskResponse;
import com.kiosk.branch.kiosk.dto.KioskCreateRequest;
import com.kiosk.branch.kiosk.dto.KioskStatusRequest;
import com.kiosk.branch.kiosk.service.BranchKioskService;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
@RequestMapping("/branch/kiosk")
public class BranchKioskController {


    private final BranchKioskService kioskService;



    /*
     * 지점 키오스크 조회
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
     * 키오스크 상태 변경
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
    
    @PostMapping
    public ResponseEntity<Void> createKiosk(
            @RequestBody KioskCreateRequest request
    ){

        kioskService.createKiosk(request);


        return ResponseEntity.ok().build();

    }

}