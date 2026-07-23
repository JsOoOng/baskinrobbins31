package com.kiosk.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.ExpenseCategory;
import com.kiosk.entity.enums.ExpensePaymentMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] StoreExpense
 *
 * <p>역할: 지출 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 STORE_EXPENSES 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_id, employee_id, staff_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "STORE_EXPENSES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreExpense {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Integer id;


    /*
     * 지점
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_id",
            nullable = false
    )
    private Store store;


    /*
     * 지출 등록 관리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;


    /*
     * 급여 대상 직원
     *
     * 일반 지출은 NULL
     * 급여 지출만 Staff 연결
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "staff_id"
    )
    private Staff staff;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "expense_category",
            nullable = false
    )
    private ExpenseCategory expenseCategory;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false
    )
    private ExpensePaymentMethod paymentMethod;


    @Column(
            name = "expense_date",
            nullable = false
    )
    private LocalDate expenseDate;


    /*
     * 지출 금액
     *
     * 급여 지급 시 계산된 급여 저장
     */
    @Column(
            name = "amount",
            nullable = false
    )
    private Integer amount;


    @Column(
            name = "description",
            nullable = false
    )
    private String description;


    @Column(name = "receipt_url")
    private String receiptUrl;


    @CreationTimestamp
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;

}