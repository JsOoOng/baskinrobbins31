package com.kiosk.branch.kiosk.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.kiosk.dto.BranchKioskResponse;
import com.kiosk.branch.kiosk.dto.KioskCreateRequest;
import com.kiosk.branch.kiosk.dto.KioskStatusRequest;
import com.kiosk.branch.kiosk.repository.BranchKioskRepository;
import com.kiosk.branch.kiosk.repository.StoreRepository;
import com.kiosk.entity.Kiosk;
import com.kiosk.entity.Store;
import com.kiosk.entity.enums.KioskStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchKioskService {


    private final BranchKioskRepository kioskRepository;
    private final StoreRepository storeRepository;
    
    private final Map<Integer, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer storeId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간
        emitters.computeIfAbsent(storeId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(storeId, emitter));
        emitter.onTimeout(() -> removeEmitter(storeId, emitter));
        emitter.onError((e) -> removeEmitter(storeId, emitter));

        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("connected"));
        } catch (IOException e) {
            removeEmitter(storeId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Integer storeId, SseEmitter emitter) {
        List<SseEmitter> storeEmitters = emitters.get(storeId);
        if (storeEmitters != null) {
            storeEmitters.remove(emitter);
        }
    }

    private void notifyStore(Integer storeId) {
        List<SseEmitter> storeEmitters = emitters.get(storeId);
        if (storeEmitters != null) {
            for (SseEmitter emitter : storeEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("KIOSK_UPDATE").data("update"));
                } catch (IOException e) {
                    storeEmitters.remove(emitter);
                }
            }
        }
    }


    /*
     * 지점 키오스크 목록 조회
     */
    public List<BranchKioskResponse> getKiosks(
            Integer storeId
    ){

        return kioskRepository.findByStore_Id(storeId)
                .stream()
                .map(BranchKioskResponse::from)
                .toList();

    }



    /*
     * 키오스크 상태 변경
     */
    @Transactional
    public void updateStatus(
            Integer kioskId,
            KioskStatusRequest request
    ){

        Kiosk kiosk =
                kioskRepository.findById(kioskId)
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "키오스크를 찾을 수 없습니다."
                    )
                );


        kiosk.changeStatus(
                request.getStatus()
        );
        
        notifyStore(kiosk.getStore().getId());

    }
    
    @Transactional
    public void createKiosk(
            KioskCreateRequest request
    ){


        // 시리얼 중복 확인
        if(
            kioskRepository.existsByDeviceSerial(
                request.getDeviceSerial()
            )
        ){

            throw new IllegalArgumentException(
                "이미 등록된 장비입니다."
            );

        }



        // 지점 내 번호 중복 확인
        if(
            kioskRepository.existsByStore_IdAndKioskNumber(
                request.getStoreId(),
                request.getKioskNumber()
            )
        ){

            throw new IllegalArgumentException(
                "이미 존재하는 키오스크 번호입니다."
            );

        }



        Store store =
                storeRepository.findById(
                    request.getStoreId()
                )
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "존재하지 않는 지점입니다."
                    )
                );


        Kiosk kiosk = Kiosk.builder()

                .store(store)

                .kioskNumber(
                    request.getKioskNumber()
                )

                .deviceSerial(
                    request.getDeviceSerial()
                )

                .kioskStatus(
                    KioskStatus.OFFLINE
                )

                .build();



        kioskRepository.save(kiosk);
        notifyStore(request.getStoreId());


    }

}