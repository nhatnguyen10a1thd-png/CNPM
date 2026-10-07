package com.thinh.cosmetic.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
    private MockMvc mvc;
    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ErrorController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void validationHasFields() throws Exception {
        mvc.perform(post("/errors/validation").contentType("application/json").content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
    @Test void notFoundAndConflictHaveSemanticStatus() throws Exception {
        mvc.perform(get("/errors/missing")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(get("/errors/conflict")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
    }
    @Test void unexpectedDoesNotDiscloseInternals() throws Exception {
        mvc.perform(get("/errors/unexpected")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-secret"))));
    }
    @RestController static class ErrorController {
        @PostMapping("/errors/validation") void validation(@Valid @RequestBody Payload payload) { }
        @GetMapping("/errors/missing") void missing() { throw new NotFoundException("Không tìm thấy."); }
        @GetMapping("/errors/conflict") void conflict() { throw new ConflictException("Dữ liệu bị trùng."); }
        @GetMapping("/errors/unexpected") void unexpected() { throw new IllegalStateException("private-secret"); }
    }
    record Payload(@NotBlank String name) { }
}
