package com.example.withdog.mission.application.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyMissionSummaryResponse(
        LocalDate weekStartDate,
        int gaugePoints,   // 달성한 미션 기준으로 차오르는 주간 게이지 (경험치 수령 여부와 무관)
        int totalPoints,
        int level,
        int experience,
        List<MissionResponse> missions
) {
}
