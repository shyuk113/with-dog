package com.example.withdog.schedule.presentation;

import com.example.withdog.schedule.application.ScheduleService;
import com.example.withdog.schedule.application.dto.request.CreateScheduleRequest;
import com.example.withdog.schedule.application.dto.request.UpdateScheduleRequest;
import com.example.withdog.schedule.application.dto.request.UpdateStatus;
import com.example.withdog.schedule.application.dto.response.ScheduleResponse;
import com.example.withdog.schedule.domain.ScheduleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    //일정 등록
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(request, userId));
    }

    //캘린더 조회 (예: ?from=2026-11-01&to=2026-11-30, 강아지/종류 필터는 선택)
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@AuthenticationPrincipal Long userId,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                               @RequestParam(required = false) Long dogId,
                                                               @RequestParam(required = false) ScheduleType type) {
        return ResponseEntity.ok(scheduleService.getSchedules(userId, from, to, dogId, type));
    }

    //다가오는 일정
    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleResponse>> getUpcomingSchedules(@AuthenticationPrincipal Long userId,
                                                                       @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(scheduleService.getUpcomingSchedules(userId, limit));
    }

    //상세
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getDetailSchedule(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(scheduleService.getSchedule(id, userId));
    }

    //수정
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable Long id, @Valid @RequestBody UpdateScheduleRequest request, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(scheduleService.updateSchedule(request, id, userId));
    }

    //다녀옴 / 취소 처리
    @PatchMapping("/{id}/status")
    public ResponseEntity<ScheduleResponse> updateScheduleStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatus request, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(scheduleService.updateScheduleStatus(request, id, userId));
    }

    //삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        scheduleService.deleteSchedule(id, userId);
        return ResponseEntity.noContent().build();
    }
}
