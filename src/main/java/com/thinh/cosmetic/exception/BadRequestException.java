package com.thinh.cosmetic.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) { this("INVALID_REQUEST", message); }
    public BadRequestException(String code, String message) { super(HttpStatus.BAD_REQUEST, code, message); }
}
