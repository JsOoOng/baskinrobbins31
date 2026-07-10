package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

@Repository
public interface HeadFlavorMapper extends JpaRepository<IcecreamFlavor, Integer> {

    List<IcecreamFlavor> findAllByOrderByIdDesc();

    List<IcecreamFlavor> findByIsActiveTrueOrderByIdDesc();

    boolean existsByFlavorName(String flavorName);

    boolean existsByFlavorNameAndIdNot(String flavorName, Integer flavorId);
}

/*
findAllByOrderByIdDesc()
→ 본사 관리용 전체 맛 목록 조회

findByIsActiveTrueOrderByIdDesc()
→ 운영 중인 맛만 조회할 때 사용 가능

existsByFlavorName()
→ 등록 시 맛 이름 중복 확인

existsByFlavorNameAndIdNot()
→ 수정 시 자기 자신 제외 중복 확인
*/