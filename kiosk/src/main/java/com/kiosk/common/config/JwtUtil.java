package com.kiosk.common.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final String SECRET =
            "mySecretKeymySecretKeymySecretKey123456";

    private final long EXPIRATION_TIME =
            1000L * 60 * 60 * 20; // 20시간

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
                                        + EXPIRATION_TIME
                        )
                )
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
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
                                        + EXPIRATION_TIME
                        )
                )
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
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
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
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