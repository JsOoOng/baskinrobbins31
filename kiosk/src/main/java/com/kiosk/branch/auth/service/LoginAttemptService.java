package com.kiosk.branch.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 실패 횟수 및 일시적인 로그인 차단을 관리한다.
 *
 * <p>
 * Redis를 사용하지 않고 서버 메모리(ConcurrentHashMap)에 상태를 저장한다.
 * </p>
 *
 * <p>정책</p>
 * <ul>
 *     <li>동일 로그인 ID 기준으로 실패 횟수를 관리한다.</li>
 *     <li>5회 연속 로그인 실패 시 10분간 로그인을 차단한다.</li>
 *     <li>차단 시간이 지나면 자동으로 상태를 초기화한다.</li>
 *     <li>로그인에 성공하면 실패 횟수를 즉시 초기화한다.</li>
 * </ul>
 */
@Service
@Slf4j
public class LoginAttemptService {

    /**
     * 최대 로그인 실패 허용 횟수
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 로그인 차단 시간
     * 10분
     */
    private static final Duration BLOCK_DURATION =
            Duration.ofMinutes(10);

    /**
     * 로그인 ID별 실패 정보를 저장한다.
     *
     * key   : loginId
     * value : 로그인 실패 정보
     */
    private final ConcurrentHashMap<String, LoginAttempt> attempts =
            new ConcurrentHashMap<>();


    /**
     * 현재 로그인 ID가 차단되어 있는지 확인한다.
     *
     * @param loginId 로그인 ID
     * @return 차단되어 있으면 true
     */
    public boolean isBlocked(String loginId) {

        if (loginId == null || loginId.isBlank()) {
            return false;
        }

        LoginAttempt attempt = attempts.get(loginId);

        if (attempt == null) {
            return false;
        }

        /*
         * 차단 상태가 아니면 로그인 가능
         */
        if (attempt.blockedUntil == null) {
            return false;
        }

        /*
         * 차단 시간이 지났으면 자동 초기화
         */
        if (Instant.now().isAfter(attempt.blockedUntil)) {
            attempts.remove(loginId);
            return false;
        }

        /*
         * 아직 차단 시간이 남아 있음
         */
        return true;
    }


    /**
     * 로그인 실패를 기록한다.
     *
     * @param loginId 로그인 ID
     */
    public void loginFailed(String loginId) {

        if (loginId == null || loginId.isBlank()) {
            return;
        }

        attempts.compute(loginId, (key, oldAttempt) -> {

            /*
             * 기존 실패 기록이 없으면 새로 생성
             */
            if (oldAttempt == null) {
                oldAttempt = new LoginAttempt();
            }

            /*
             * 이미 차단 시간이 끝났다면
             * 새로운 로그인 시도로 다시 시작
             */
            if (oldAttempt.blockedUntil != null
                    && Instant.now().isAfter(oldAttempt.blockedUntil)) {

                oldAttempt.failedCount = 0;
                oldAttempt.blockedUntil = null;
            }

            /*
             * 실패 횟수 증가
             */
            oldAttempt.failedCount++;
            
            log.warn(
            	    "[LOGIN FAILED] loginId={} | 실패 횟수={}/{}",
            	    loginId,
            	    oldAttempt.failedCount,
            	    MAX_ATTEMPTS
            	);
            

            /*
             * 최대 실패 횟수에 도달하면 차단
             */
            if (oldAttempt.failedCount >= MAX_ATTEMPTS) {

                oldAttempt.blockedUntil =
                        Instant.now().plus(BLOCK_DURATION);

                log.warn(
                    "[LOGIN BLOCK] loginId={} | {}회 실패 → 10분 차단",
                    loginId,
                    MAX_ATTEMPTS
                );
            }

            return oldAttempt;
        });
    }


    /**
     * 로그인 성공 시 해당 ID의 실패 기록을 초기화한다.
     *
     * @param loginId 로그인 ID
     */
    public void loginSucceeded(String loginId) {

        if (loginId == null || loginId.isBlank()) {
            return;
        }

        attempts.remove(loginId);
        
        log.info(
        	    "[LOGIN SUCCESS] loginId={} | 실패 기록 초기화",
        	    loginId
        	);
    }


    /**
     * 현재 로그인 ID의 실패 횟수를 반환한다.
     *
     * 테스트나 관리자 모니터링 용도로 사용할 수 있다.
     *
     * @param loginId 로그인 ID
     * @return 실패 횟수
     */
    public int getFailedCount(String loginId) {

        LoginAttempt attempt = attempts.get(loginId);

        if (attempt == null) {
            return 0;
        }

        /*
         * 차단 시간이 끝났으면 초기화
         */
        if (attempt.blockedUntil != null
                && Instant.now().isAfter(attempt.blockedUntil)) {

            attempts.remove(loginId);
            
            log.info(
                    "[LOGIN UNBLOCK] loginId={} | 10분 차단 해제",
                    loginId
                );
            
            return 0;
        }

        return attempt.failedCount;
    }


    /**
     * 로그인 실패 상태를 저장하는 내부 클래스
     */
    private static class LoginAttempt {

        /**
         * 로그인 실패 횟수
         */
        private int failedCount;

        /**
         * 로그인 차단 종료 시각
         *
         * null이면 현재 차단 상태가 아님
         */
        private Instant blockedUntil;
    }
}