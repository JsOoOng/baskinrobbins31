package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "phone", length = 20, nullable = false, unique = true)
    private String phone;

    @Column(name = "point_balance", nullable = false)
    @Builder.Default
    private Integer pointBalance = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 포인트 더하기
    public void addPoint(int amount) {
        if (this.pointBalance == null) {
            this.pointBalance = 0;
        }
        this.pointBalance += amount;
    }

    // 포인트 빼기
    public void deductPoints(int amount) {
        this.pointBalance -= amount;
      
    
}