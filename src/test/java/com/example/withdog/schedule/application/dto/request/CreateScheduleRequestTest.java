package com.example.withdog.schedule.application.dto.request;

import com.example.withdog.schedule.domain.ScheduleType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateScheduleRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 필수값만_있으면_통과한다() {
        CreateScheduleRequest request = new CreateScheduleRequest(null, ScheduleType.HOSPITAL, "건강검진",
                null, null, LocalDateTime.of(2026, 11, 20, 10, 0), null, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void 종류_제목_시작시간이_없으면_실패한다() {
        CreateScheduleRequest request = new CreateScheduleRequest(null, null, " ", null, null, null, null, null);

        Set<ConstraintViolation<CreateScheduleRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("type", "title", "startAt");
    }

    @Test
    void 제목과_메모가_너무_길면_실패한다() {
        CreateScheduleRequest request = new CreateScheduleRequest(null, ScheduleType.GROOMING, "가".repeat(101),
                null, "가".repeat(501), LocalDateTime.of(2026, 11, 20, 10, 0), null, null);

        assertThat(validator.validate(request)).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("title", "memo");
    }
}
