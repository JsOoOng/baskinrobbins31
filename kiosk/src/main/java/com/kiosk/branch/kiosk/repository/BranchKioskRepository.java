package com.kiosk.branch.kiosk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Kiosk;


/**
 * [코드 흐름 안내] BranchKioskRepository
 *
 * <p>역할: 지점 운영의 키오스크 기기 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(KIOSKS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface BranchKioskRepository 
        extends JpaRepository<Kiosk, Integer> {


    // 지점별 키오스크 조회
    List<Kiosk> findByStore_Id(Integer storeId);


    // 같은 시리얼 번호 존재 여부
    boolean existsByDeviceSerial(String deviceSerial);


    // 같은 지점 키오스크 번호 중복 확인
    boolean existsByStore_IdAndKioskNumber(
            Integer storeId,
            Integer kioskNumber
    );

}