package com.kiosk.branch.expense.dto;

import java.time.LocalDate;

import com.kiosk.entity.enums.ExpenseCategory;
import com.kiosk.entity.enums.ExpensePaymentMethod;

import lombok.Getter;
import lombok.Setter;

/**
 * [코드 흐름 안내] StoreExpenseRequestDTO
 *
 * <p>역할: 지출 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
public class StoreExpenseRequestDTO {


    /*
     * 지점 ID
     */
    private Integer storeId;


    /*
     * 지출 등록 관리자 ID
     */
    private Integer employeeId;


    /*
     * 급여 지출일 경우 직원 ID
     * 일반 지출은 null
     */
    private Integer staffId;


    /*
     * 지출 분류
     * RENT, UTILITY, LABOR,
     * SUPPLIES, MAINTENANCE,
     * INGREDIENTS, INSURANCE, ETC
     */
    private ExpenseCategory expenseCategory;


    /*
     * 결제 방식
     * CARD, CASH, TRANSFER
     */
    private ExpensePaymentMethod paymentMethod;


    /*
     * 지출 날짜
     */
    private LocalDate expenseDate;


    /*
     * 지출 금액
     */
    private Integer amount;


    /*
     * 지출 내용
     */
    private String description;


    /*
     * 영수증 이미지 주소
     */
    private String receiptUrl;

}