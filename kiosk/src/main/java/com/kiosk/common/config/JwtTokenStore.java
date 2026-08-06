package com.kiosk.common.config;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenStore {

    private final ConcurrentHashMap<String, String> tokens =
            new ConcurrentHashMap<>();


    public void save(
            String key,
            String token
    ) {
        tokens.put(key, token);
    }


    public boolean isValid(
            String key,
            String token
    ) {

        String savedToken =
                tokens.get(key);

        return token.equals(savedToken);
    }


    public void remove(
            String key
    ) {
        tokens.remove(key);
    }
}