package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.inquiry.HeadInquiryAnswerRequestDTO;
import com.kiosk.headquarter.dto.inquiry.HeadInquiryDetailResponseDTO;
import com.kiosk.headquarter.dto.inquiry.HeadInquiryListResponseDTO;
import com.kiosk.headquarter.service.HeadInquiryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadInquiryController {

    private final HeadInquiryService headInquiryService;

    // 문의 전체 목록 조회
    @GetMapping("/head/inquiries")
    public List<HeadInquiryListResponseDTO> getInquiryList() {

        return headInquiryService.getInquiryList();
    }

    // 답변 대기 문의 목록 조회
    @GetMapping("/head/inquiries/waiting")
    public List<HeadInquiryListResponseDTO> getWaitingInquiryList() {

        return headInquiryService.getWaitingInquiryList();
    }

    // 문의 상세 조회
    @GetMapping("/head/inquiries/{inquiryId}")
    public HeadInquiryDetailResponseDTO getInquiryDetail(
            @PathVariable Integer inquiryId) {

        return headInquiryService.getInquiryDetail(inquiryId);
    }

    // 문의 답변 등록
    @PutMapping("/head/inquiries/{inquiryId}/answer")
    public String answerInquiry(
            @PathVariable Integer inquiryId,
            @RequestBody HeadInquiryAnswerRequestDTO requestDTO) {

        return headInquiryService.answerInquiry(inquiryId, requestDTO);
    }
}