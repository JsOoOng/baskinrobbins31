package com.kiosk.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.StaffStatus;

import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] Staff
 *
 * <p>역할: 직원 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 STAFFS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "STAFFS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "hp", length = 20, nullable = false)
    private String hp;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "health_cert_end_date")
    private LocalDate healthCertEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StaffStatus status = StaffStatus.WORKING;

    @Column(name = "hourly_wage", nullable = false)
    private Integer hourlyWage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void update(
            String name,
            String hp,
            String email,
            String address,
            LocalDate birthDate,
            LocalDate healthCertEndDate,
            Integer hourlyWage,
            StaffStatus status
    ) {
        this.name = name;
        this.hp = hp;
        this.email = email;
        this.address = address;
        this.birthDate = birthDate;
        this.healthCertEndDate = healthCertEndDate;
        this.hourlyWage = hourlyWage;
        this.status = status;
    }

    public void changeStatus(StaffStatus status) {
        this.status = status;
    }

}