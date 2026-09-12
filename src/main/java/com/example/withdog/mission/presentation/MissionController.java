package com.example.withdog.mission.presentation;

import com.example.withdog.mission.application.MissionService;
import com.example.withdog.mission.application.dto.WeeklyMissionSummaryResponse;
import com.example.withdog.mission.domain.MissionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    //주간 미션 목록 조회 (저장된 상태만 읽음, 재계산 없음)
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyMissionSummaryResponse> getWeeklyMissions(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(missionService.getWeeklyMissions(userId));
    }

    //달성한 미션의 완료(수령) 버튼 -> 경험치 지급
    @PostMapping("/weekly/{missionType}/claim")
    public ResponseEntity<WeeklyMissionSummaryResponse> claimMission(@PathVariable MissionType missionType,
                                                                       @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(missionService.claimMission(userId, missionType));
    }
}
