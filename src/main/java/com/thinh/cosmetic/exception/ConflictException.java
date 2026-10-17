package com.thinh.cosmetic.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(String message) { this("CONFLICT", message); }
    public ConflictException(String code, String message) { super(HttpStatus.CONFLICT, code, message); }
}
