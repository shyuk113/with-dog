package com.example.withdog.schedule.application.dto.request;

import com.example.withdog.schedule.domain.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

//dogId, placeName, memo, endAt, allDay는 선택 (상태는 항상 SCHEDULED로 시작하므로 받지 않는다)
//allDay=true면 시간은 무시하고 날짜만 사용: startAt = 첫날, endAt = 마지막 날(없으면 하루짜리)
//  저장/응답은 [첫날 00:00, 마지막 날 다음 날 00:00) 형태 (예: 11/5 종일 -> endAt 11/6 00:00)
public record CreateScheduleRequest(
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
