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

/**
 * [코드 흐름 안내] HeadInquiryService
 *
 * <p>역할: 본사 관리의 문의 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadInquiryMapper, HeadquarterAdminMapper -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadInquiryService {

    private final HeadInquiryMapper headInquiryMapper;
    private final HeadquarterAdminMapper headquarterAdminMapper;
    private final AdminLogService adminLogService;

    // 문의 전체 목록 조회
    /**
     * [메서드 흐름] getInquiryList
     * Controller 또는 상위 서비스에서 호출되어 HeadInquiryMapper, HeadquarterAdminMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadInquiryListResponseDTO> getInquiryList() {

        return headInquiryMapper.findAllByOrderByIdDesc()
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 답변 대기 문의 목록 조회
    /**
     * [메서드 흐름] getWaitingInquiryList
     * Controller 또는 상위 서비스에서 호출되어 HeadInquiryMapper, HeadquarterAdminMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadInquiryListResponseDTO> getWaitingInquiryList() {

        return headInquiryMapper.findByStatusOrderByIdDesc(InquiryStatus.WAITING)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 문의 상세 조회
    /**
     * [메서드 흐름] getInquiryDetail
     * Controller 또는 상위 서비스에서 호출되어 HeadInquiryMapper, HeadquarterAdminMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadInquiryDetailResponseDTO getInquiryDetail(Integer inquiryId) {

        Inquiry inquiry = headInquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        return toDetailResponseDTO(inquiry);
    }

    // 문의 답변 등록
    @Transactional
    /**
     * [메서드 흐름] answerInquiry
     * Controller 또는 상위 서비스에서 호출되어 HeadInquiryMapper, HeadquarterAdminMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

        adminLogService.logAction("고객 문의", "문의 답변 등록 (ID: " + inquiryId + ")");
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
