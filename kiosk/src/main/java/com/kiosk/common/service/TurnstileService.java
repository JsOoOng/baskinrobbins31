package com.kiosk.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class TurnstileService {

    @Value("${cloudflare.turnstile.secret-key}")
    private String turnstileSecretKey;

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestTemplate restTemplate;

    public TurnstileService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Turnstile 토큰을 검증합니다.
     * @param token 프론트엔드에서 전달받은 turnstileToken
     * @return 검증 성공 여부
     */
    public boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Turnstile token is empty");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("secret", turnstileSecretKey);
            map.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    TURNSTILE_VERIFY_URL,
                    request,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("success")) {
                boolean success = (Boolean) response.getBody().get("success");
                if (!success) {
                    log.warn("Turnstile verification failed: {}", response.getBody().get("error-codes"));
                }
                return success;
            }

            return false;
        } catch (Exception e) {
            log.error("Turnstile verification error", e);
            return false;
        }
    }
}
