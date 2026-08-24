package com.kiosk.branch.auth.exception;

/**
 * 로그인 실패 횟수 초과 및 로그인 실패를 나타내는 예외
 */
public class LoginAttemptException extends RuntimeException {

    private final int failedAttempts;
    private final int maxAttempts;
    private final boolean blocked;

    public LoginAttemptException(
            String message,
            int failedAttempts,
            int maxAttempts,
            boolean blocked
    ) {
        super(message);
        this.failedAttempts = failedAttempts;
        this.maxAttempts = maxAttempts;
        this.blocked = blocked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean isBlocked() {
        return blocked;
    }
}