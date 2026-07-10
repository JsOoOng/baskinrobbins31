package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.Inquiry;
import com.kiosk.entity.enums.InquiryStatus;
import com.kiosk.headquarter.dto.inquiry.HeadInquiryAnswerRequestDTO;
import com.kiosk.headquarter.dto.inquiry.HeadInquiryDetailResponseDTO;
import com.kiosk.headquarter.dto.inquiry.HeadInquiryListResponseDTO;
import com.kiosk.headquarter.repository.HeadInquiryMapper;
import com.kiosk.headquarter.repository.HeadquarterAdminMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadInquiryService {

    private final HeadInquiryMapper headInquiryMapper;
    private final HeadquarterAdminMapper headquarterAdminMapper;

    // 문의 전체 목록 조회
    public List<HeadInquiryListResponseDTO> getInquiryList() {

        return headInquiryMapper.findAllByOrderByIdDesc()
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 답변 대기 문의 목록 조회
    public List<HeadInquiryListResponseDTO> getWaitingInquiryList() {

        return headInquiryMapper.findByStatusOrderByIdDesc(InquiryStatus.WAITING)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 문의 상세 조회
    public HeadInquiryDetailResponseDTO getInquiryDetail(Integer inquiryId) {

        Inquiry inquiry = headInquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        return toDetailResponseDTO(inquiry);
    }

    // 문의 답변 등록
    @Transactional
    public String answerInquiry(Integer inquiryId, HeadInquiryAnswerRequestDTO requestDTO) {

        Inquiry inquiry = headInquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        if (requestDTO.getAdminId() == null) {
            throw new IllegalArgumentException("답변 작성자 adminId가 필요합니다.");
        }

        HeadquarterAdmin admin = headquarterAdminMapper.findById(requestDTO.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));

        if (!admin.isActiveAdmin()) {
            throw new IllegalArgumentException("비활성화된 본사 관리자는 답변할 수 없습니다.");
        }

        if (requestDTO.getAnswer() == null || requestDTO.getAnswer().isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        inquiry.writeAnswer(requestDTO.getAnswer(), admin);

        return "문의 답변 등록 성공";
    }

    private HeadInquiryListResponseDTO toListResponseDTO(Inquiry inquiry) {

        return HeadInquiryListResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .inquiryType(inquiry.getInquiryType())
                .storeId(inquiry.getStore().getId())
                .storeName(inquiry.getStore().getStoreName())
                .title(inquiry.getTitle())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }

    private HeadInquiryDetailResponseDTO toDetailResponseDTO(Inquiry inquiry) {

        Integer adminId = null;
        String adminName = null;

        if (inquiry.getAdmin() != null) {
            adminId = inquiry.getAdmin().getId();
            adminName = inquiry.getAdmin().getName();
        }

        return HeadInquiryDetailResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .inquiryType(inquiry.getInquiryType())
                .storeId(inquiry.getStore().getId())
                .storeName(inquiry.getStore().getStoreName())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .answer(inquiry.getAnswer())
                .status(inquiry.getStatus())
                .adminId(adminId)
                .adminName(adminName)
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}