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