package com.example.withdog.auth.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignupRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 정상적인_값이면_검증을_통과한다() {
        SignupRequest request = new SignupRequest("보호자", "guardian@example.com", "password123", "서울");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void 이메일_형식이_아니면_검증에_실패한다() {
        SignupRequest request = new SignupRequest("보호자", "not-an-email", "password123", "서울");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 빈_문자열_이메일이나_비밀번호는_검증에_실패한다() {
        SignupRequest request = new SignupRequest("", "", "", "");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void 비밀번호가_8자_미만이면_검증에_실패한다() {
        SignupRequest request = new SignupRequest("보호자", "guardian@example.com", "1234567", "서울");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
