package com.kiosk.common.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * [코드 흐름 안내] JwtUtil
 *
 * <p>역할: 공통 기반 기능의 JWT 인증 처리를 보조하는 class다.</p>
 * <p>호출 흐름: 호출하는 클래스에서 필요한 값을 받아 현재 메서드의 결과를 다음 계층으로 전달한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Component
public class JwtUtil {

    private static final int MINIMUM_SECRET_BYTES = 32;

    private final String secret;
    private final long expirationTime;
    private SecretKey signingKey;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-time:72000000}") long expirationTime
    ) {
        this.secret = secret;
        this.expirationTime = expirationTime;
    }

    @PostConstruct
    void validateConfiguration() {
        byte[] secretBytes = secret == null
                ? new byte[0]
                : secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "JWT_SECRET은 UTF-8 기준 32바이트 이상이어야 합니다."
            );
        }

        if (expirationTime <= 0) {
            throw new IllegalArgumentException(
                    "JWT 만료 시간은 0보다 커야 합니다."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    // 지점 관리자 JWT 생성
    public String createToken(AuthResponse user) {

        return Jwts.builder()
                .subject(String.valueOf(user.getEmployeeId()))

                // enum일 가능성을 고려해 문자열로 변환
                .claim("role", String.valueOf(user.getRole()))

                .claim("storeId", user.getStoreId())
                .claim("userType", "BRANCH")
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationTime
                        )
                )
                .signWith(
                        signingKey
                )
                .compact();
    }

    // 본사 관리자 JWT 생성
    public String createToken(HeadLoginResponse user) {

        return Jwts.builder()
                .subject(String.valueOf(user.getEmployeeId()))
                .claim("loginId", user.getLoginId())

                // HEAD_ADMIN을 ADMIN으로 바꾸지 않고 그대로 저장
                .claim("role", user.getRole())

                .claim("storeId", user.getStoreId())
                .claim("userType", "HEAD")
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationTime
                        )
                )
                .signWith(
                        signingKey
                )
                .compact();
    }

    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // 전체 Claims 추출
    public Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(
                        signingKey
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 직원 ID 추출
    public Integer getEmployeeId(String token) {

        Claims claims = getClaims(token);

        return Integer.parseInt(
                claims.getSubject()
        );
    }

    // 로그인 ID 추출
    public String getLoginId(String token) {

        Claims claims = getClaims(token);

        return claims.get(
                "loginId",
                String.class
        );
    }

    // 역할 추출
    public String getRole(String token) {

        Claims claims = getClaims(token);

        return claims.get(
                "role",
                String.class
        );
    }

    // 사용자 유형 추출
    public String getUserType(String token) {

        Claims claims = getClaims(token);

        return claims.get(
                "userType",
                String.class
        );
    }

    // 지점 ID 추출
    public Integer getStoreId(String token) {

        Claims claims = getClaims(token);

        return claims.get(
                "storeId",
                Integer.class
        );
    }
}
