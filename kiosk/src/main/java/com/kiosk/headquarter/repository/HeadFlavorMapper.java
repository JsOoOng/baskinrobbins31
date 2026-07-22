package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

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