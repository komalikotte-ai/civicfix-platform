package com.civicfix.platform.common.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized error payload returned across all CivicFix failure responses.
 */
public class ApiErrorResponse {

    private boolean success;
    private int statusCode;
    private String errorCode;
    private String message;
    private String path;
    private String requestId;
    private List<ValidationErrorDetail> validationErrors;
    private Instant timestamp;

    public ApiErrorResponse() {
        this.success = false;
        this.requestId = CorrelationIdContext.get();
        this.timestamp = Instant.now();
        this.validationErrors = new ArrayList<>();
    }

    public ApiErrorResponse(int statusCode, String errorCode, String message, String path) {
        this.success = false;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.requestId = CorrelationIdContext.get();
        this.timestamp = Instant.now();
        this.validationErrors = new ArrayList<>();
    }

    public ApiErrorResponse(int statusCode, String errorCode, String message, String path, List<ValidationErrorDetail> validationErrors) {
        this.success = false;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.requestId = CorrelationIdContext.get();
        this.timestamp = Instant.now();
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<ValidationErrorDetail> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationErrorDetail> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
