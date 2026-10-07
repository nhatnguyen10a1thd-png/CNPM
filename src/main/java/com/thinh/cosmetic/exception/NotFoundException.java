package com.thinh.cosmetic.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) { this("NOT_FOUND", message); }
    public NotFoundException(String code, String message) { super(HttpStatus.NOT_FOUND, code, message); }
}
