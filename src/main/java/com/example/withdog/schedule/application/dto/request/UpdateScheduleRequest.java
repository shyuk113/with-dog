package com.example.withdog.schedule.application.dto.request;

import com.example.withdog.schedule.domain.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

//전체 수정(PUT): 보내지 않은 선택 항목은 비워진다 (dogId를 빼면 강아지 연결 해제)
public record UpdateScheduleRequest(
        Long dogId,
        @NotNull ScheduleType type,
        @NotBlank @Size(max = 100) String title,
        @Size(max = 100) String placeName,
        @Size(max = 500) String memo,
        @NotNull LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean allDay
) {
}
