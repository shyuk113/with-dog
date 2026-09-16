package com.example.withdog.dog.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateHealthConditionsRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void healthConditions가_null이면_검증에_실패한다() {
        UpdateHealthConditionsRequest request = new UpdateHealthConditionsRequest(null);

        Set<ConstraintViolation<UpdateHealthConditionsRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void healthConditions가_빈_리스트여도_검증을_통과한다() {
        UpdateHealthConditionsRequest request = new UpdateHealthConditionsRequest(List.of());

        Set<ConstraintViolation<UpdateHealthConditionsRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
