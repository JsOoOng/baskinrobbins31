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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "headquarter_admins")
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
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public boolean isActiveAdmin() {
        return this.status == AdminStatus.ACTIVE;
    }

    public boolean isSuperAdmin() {
        return this.role == AdminRole.SUPER_ADMIN;
    }

    public void changeRole(AdminRole role) {
        this.role = role;
    }

    public void changeStatus(AdminStatus status) {
        this.status = status;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
    
    /*
     * 관리자 기본 정보 수정
     *
     * 로그인 ID는 변경하지 않습니다.
     */
    public void updateAdmin(
            String name,
            String department,
            AdminRole role
    ) {
        this.name = name;
        this.department = department;
        this.role = role;
    }

    @PrePersist
    private void prePersist() {

        if (this.createdAt == null) {
            this.createdAt =
                    LocalDateTime.now();
        }

        if (this.status == null) {
            this.status =
                    AdminStatus.ACTIVE;
        }
    }
}