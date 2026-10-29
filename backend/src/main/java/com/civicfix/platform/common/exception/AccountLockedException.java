package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends BaseApiException {

    public AccountLockedException(String message) {
        super(message, HttpStatus.LOCKED, "ACCOUNT_LOCKED");
    }

    public AccountLockedException(long minutesRemaining) {
        super(String.format("Account is locked due to too many failed login attempts. Please try again in %d minute(s).", minutesRemaining),
                HttpStatus.LOCKED, "ACCOUNT_LOCKED");
    }
}
