package com.civicfix.platform.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseApiException {

    public BusinessException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, errorCode);
    }
}
