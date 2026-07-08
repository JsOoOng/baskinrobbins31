package com.kiosk.entity;

import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
}