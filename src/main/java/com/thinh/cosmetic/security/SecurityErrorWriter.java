package com.thinh.cosmetic.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

public final class SecurityErrorWriter {
    private SecurityErrorWriter() { }

    public static void write(ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response,
                             int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), Map.of("timestamp", Instant.now().toString(), "status", status,
                "code", code, "message", message, "path", request.getRequestURI(), "fieldErrors", Map.of()));
    }
}
