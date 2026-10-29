package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseApiException {

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }

    public InvalidTokenException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
