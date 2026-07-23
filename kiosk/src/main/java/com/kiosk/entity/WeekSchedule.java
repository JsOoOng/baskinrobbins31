package com.kiosk.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.kiosk.entity.enums.WorkDay;

import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] WeekSchedule
 *
 * <p>역할: 근무 일정 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 WEEK_SCHEDULES 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 staff_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(
        name = "WEEK_SCHEDULES",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "staff_id",
                        "day_of_week"
                })
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeekSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

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

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(
            LocalTime startTime,
            LocalTime endTime,
            Boolean isHoliday
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isHoliday = isHoliday;
    }

}