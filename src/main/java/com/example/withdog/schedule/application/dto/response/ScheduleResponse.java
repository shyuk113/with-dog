package com.example.withdog.schedule.application.dto.response;

import com.example.withdog.schedule.domain.Schedule;
import com.example.withdog.schedule.domain.ScheduleStatus;
import com.example.withdog.schedule.domain.ScheduleType;

import java.time.LocalDateTime;

public record ScheduleResponse(Long id, Long userId, Long dogId, ScheduleType type, String title, String placeName, String memo,
                               ScheduleStatus status, LocalDateTime startAt, LocalDateTime endAt, boolean allDay) {

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getId(),
                schedule.getUser().getId(),
                schedule.getDog() == null ? null : schedule.getDog().getId(), //강아지와 무관한 일정은 null
                schedule.getScheduleType(),
                schedule.getTitle(),
                schedule.getPlaceName(),
                schedule.getMemo(),
                schedule.getScheduleStatus(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.isAllDay());
    }
}
