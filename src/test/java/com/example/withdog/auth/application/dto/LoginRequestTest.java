package com.example.withdog.auth.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 정상적인_값이면_검증을_통과한다() {
        LoginRequest request = new LoginRequest("guardian@example.com", "password123");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void 이메일_형식이_아니면_검증에_실패한다() {
        LoginRequest request = new LoginRequest("not-an-email", "password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 빈_비밀번호는_검증에_실패한다() {
        LoginRequest request = new LoginRequest("guardian@example.com", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
