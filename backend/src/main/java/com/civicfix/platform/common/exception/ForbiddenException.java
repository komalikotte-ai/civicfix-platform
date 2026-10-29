package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseApiException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
