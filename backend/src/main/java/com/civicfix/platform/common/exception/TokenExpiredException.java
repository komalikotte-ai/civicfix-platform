package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseApiException {

    public TokenExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
}
