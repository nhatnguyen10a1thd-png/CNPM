package com.thinh.cosmetic.exception;

import org.springframework.http.HttpStatus;

/** Unchecked so failed business operations roll back Spring transactions. */
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
}
