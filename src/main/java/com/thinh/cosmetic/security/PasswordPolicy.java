package com.thinh.cosmetic.security;

import com.thinh.cosmetic.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class PasswordPolicy {
    private PasswordPolicy() { }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !password.matches("(?s).*[A-Za-z].*") || !password.matches("(?s).*[0-9].*")) {
            throw new BadRequestException("Mật khẩu cần ít nhất 8 ký tự, có chữ và số, tối đa 72 byte");
        }
    }

    public static void validateConfirmation(String password, String confirmation) {
        validatePassword(password);
        if (!password.equals(confirmation)) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeLoginIdentifier(String identifier) {
        String normalized = normalizeEmail(identifier);
        if (normalized == null || normalized.contains("@")) return normalized;
        try { return normalizePhone(normalized); }
        catch (BadRequestException invalidPhone) { return normalized; }
    }

    public static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String normalized = phone.trim().replaceAll("[\\s().-]", "");
        if (!normalized.matches("\\+?[0-9]{9,15}")) {
            throw new BadRequestException("Số điện thoại cần có từ 9 đến 15 chữ số");
        }
        return normalized;
    }
}
