package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

/**
 * [코드 흐름 안내] HeadFlavorMapper
 *
 * <p>역할: 본사 관리의 아이스크림 맛 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(icecream_flavors) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadFlavorMapper
        extends JpaRepository<
                IcecreamFlavor,
                Integer
        > {

    /*
     * 본사 관리용 전체 맛 목록
     *
     * 최신 등록 순서
     */
    List<IcecreamFlavor>
            findAllByOrderByIdDesc();

    /*
     * 활성화된 맛 목록
     */
    List<IcecreamFlavor>
            findByIsActiveTrueOrderByIdDesc();

    /*
     * 맛 등록 시 중복 검사
     *
     * 영문 대소문자를 구분하지 않습니다.
     */
    boolean existsByFlavorNameIgnoreCase(
            String flavorName
    );

    /*
     * 맛 수정 시 현재 맛 번호를 제외하고
     * 같은 이름이 존재하는지 검사합니다.
     */
    boolean existsByFlavorNameIgnoreCaseAndIdNot(
            String flavorName,
            Integer flavorId
    );
}