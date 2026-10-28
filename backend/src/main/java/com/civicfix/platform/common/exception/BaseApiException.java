package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base abstract exception for all CivicFix platform application errors.
 */
public abstract class BaseApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected BaseApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected BaseApiException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
