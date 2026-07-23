package com.kiosk.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.WorkDay;
import com.kiosk.entity.enums.WorkStatus;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] WorkHistory
 *
 * <p>역할: 근무 이력 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 WORK_HISTORY 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 staff_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(
        name = "WORK_HISTORY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "staff_id",
                        "work_date"
                })
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private WorkDay dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_holiday", nullable = false)
    @Builder.Default
    private Boolean isHoliday = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false)
    @Builder.Default
    private WorkStatus workStatus = WorkStatus.SCHEDULED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void update(
            LocalTime startTime,
            LocalTime endTime,
            Boolean isHoliday
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isHoliday = isHoliday;
    }

    public void changeStatus(WorkStatus workStatus) {
        this.workStatus = workStatus;
    }
    
    public void changeHoliday(
            Boolean isHoliday
    ) {
        this.isHoliday = isHoliday;
    }
    

}