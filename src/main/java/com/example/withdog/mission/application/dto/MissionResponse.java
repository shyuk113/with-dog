package com.example.withdog.mission.application.dto;

import com.example.withdog.mission.domain.MissionType;
import com.example.withdog.mission.domain.WeeklyMission;

import java.time.LocalDateTime;

public record MissionResponse(
        MissionType missionType,
        String description,
        int points,
        boolean achieved,
        LocalDateTime achievedAt,
        boolean claimed,
        LocalDateTime claimedAt
) {
    public static MissionResponse from(WeeklyMission weeklyMission) {
        return new MissionResponse(
                weeklyMission.getMissionType(),
                weeklyMission.getMissionType().getDescription(),
                MissionType.POINTS_PER_MISSION,
                weeklyMission.isAchieved(),
                weeklyMission.getAchievedAt(),
                weeklyMission.isClaimed(),
                weeklyMission.getClaimedAt()
        );
    }
}
