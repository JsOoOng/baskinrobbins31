package com.kiosk.branch.security;


import java.util.Date;

import org.springframework.stereotype.Component;

import com.kiosk.branch.auth.dto.AuthResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtUtil {


    private final String SECRET =
        "mySecretKeymySecretKeymySecretKey123456";


    // JWT 생성
    public String createToken(AuthResponse user){

        return Jwts.builder()
                .subject(
                    String.valueOf(
                        user.getEmployeeId()
                    )
                )
                .claim(
                    "role",
                    user.getRole()
                )
                .claim(
                    "storeId",
                    user.getStoreId()
                )
                .issuedAt(new Date())
                .expiration(
                    new Date(
                    System.currentTimeMillis()
                    + 1000 * 60 * 60
                    )
                )
                .signWith(
                    Keys.hmacShaKeyFor(
                        SECRET.getBytes()
                    )
                )
                .compact();
    }



    // JWT 검증
    public boolean validateToken(String token){

        try{

            Jwts.parser()
                .verifyWith(
                    Keys.hmacShaKeyFor(
                        SECRET.getBytes()
                    )
                )
                .build()
                .parseSignedClaims(token);


            return true;


        }catch(Exception e){

            return false;

        }

    }



    // 사용자 ID 추출
    public Integer getEmployeeId(String token){

        Claims claims =
            Jwts.parser()
            .verifyWith(
                Keys.hmacShaKeyFor(
                    SECRET.getBytes()
                )
            )
            .build()
            .parseSignedClaims(token)
            .getPayload();


        return Integer.parseInt(
            claims.getSubject()
        );

    }

}