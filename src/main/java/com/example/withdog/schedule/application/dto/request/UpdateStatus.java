package com.example.withdog.schedule.application.dto.request;

import com.example.withdog.schedule.domain.ScheduleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatus(@NotNull ScheduleStatus status) {
}
