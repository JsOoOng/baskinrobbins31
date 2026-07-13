package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Inquiry;
import com.kiosk.entity.enums.InquiryStatus;

@Repository
public interface HeadInquiryMapper extends JpaRepository<Inquiry, Integer> {

    List<Inquiry> findAllByOrderByIdDesc();

    List<Inquiry> findByStatusOrderByIdDesc(InquiryStatus status);
}