package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HEADQUARTER_ADMINS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HeadquarterAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer id;

    @Column(name = "login_id", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "department", length = 50)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AdminStatus status = AdminStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isActiveAdmin() {
        return this.status == AdminStatus.ACTIVE;
    }
}