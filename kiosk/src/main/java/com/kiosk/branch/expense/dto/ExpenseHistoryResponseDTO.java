package com.kiosk.branch.expense.dto;

import com.kiosk.entity.enums.ExpenseCategory;
import com.kiosk.entity.enums.ExpensePaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] ExpenseHistoryResponseDTO
 *
 * <p>역할: 지출 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class ExpenseHistoryResponseDTO {

    /*
     * 지출 내용
     */
    private String description;

    /*
     * 지출 분류
     */
    private ExpenseCategory expenseCategory;

    /*
     * 결제 방식
     */
    private ExpensePaymentMethod paymentMethod;

}