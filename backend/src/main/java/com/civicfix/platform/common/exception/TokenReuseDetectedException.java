package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

public class TokenReuseDetectedException extends BaseApiException {

    public TokenReuseDetectedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "SECURITY_ALERT_TOKEN_REUSE");
    }
}
