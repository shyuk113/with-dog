package com.example.withdog.mission.application;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.mission.application.dto.MissionResponse;
import com.example.withdog.mission.application.dto.WeeklyMissionSummaryResponse;
import com.example.withdog.mission.domain.MissionType;
import com.example.withdog.mission.domain.WeeklyMission;
import com.example.withdog.mission.infrastructure.WeeklyMissionRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.walk.domain.Walk;
import com.example.withdog.walk.infrastructure.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private static final int EXP_PER_MISSION = 20;

    private final WeeklyMissionRepository weeklyMissionRepository;
    private final WalkRepository walkRepository;
    private final UserRepository userRepository;

    //이번 주 미션 목록 조회 — 저장된 상태를 그대로 읽기만 함 (재계산 없음)
    @Transactional
    public WeeklyMissionSummaryResponse getWeeklyMissions(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        LocalDate weekStartDate = currentWeekStart();

        Map<MissionType, WeeklyMission> existingMissions = findOrCreateWeeklyMissions(user, weekStartDate);

        return toSummary(user, weekStartDate, existingMissions);
    }

    //산책 종료 등 유저 활동 발생 시 이번 주 미션 달성 여부만 갱신 (경험치는 지급하지 않음 — 유저가 완료 버튼을 눌러야 지급)
    @Transactional
    public void evaluate(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        LocalDate weekStartDate = currentWeekStart();

        Map<MissionType, WeeklyMission> existingMissions = findOrCreateWeeklyMissions(user, weekStartDate);

        List<Walk> completedWalksThisWeek = walkRepository.findByUserIdAndEndedAtIsNotNullAndStartedAtBetween(
                userId, weekStartDate.atStartOfDay(), weekStartDate.plusWeeks(1).atStartOfDay());

        double walkCount = completedWalksThisWeek.size();
        double totalDistanceKm = completedWalksThisWeek.stream()
                .mapToDouble(w -> w.getDistanceKm() == null ? 0 : w.getDistanceKm())
                .sum();
        double totalDurationMinutes = completedWalksThisWeek.stream()
                .mapToLong(Walk::getDurationMinutes)
                .sum();

        for (MissionType missionType : MissionType.values()) {
            WeeklyMission weeklyMission = existingMissions.get(missionType);
            if (!weeklyMission.isAchieved() && missionType.isAchieved(walkCount, totalDistanceKm, totalDurationMinutes)) {
                weeklyMission.achieve(LocalDateTime.now());
            }
        }
    }

    //달성한 미션의 보상(경험치)을 유저가 직접 수령
    @Transactional
    public WeeklyMissionSummaryResponse claimMission(Long userId, MissionType missionType){
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        LocalDate weekStartDate = currentWeekStart();

        Map<MissionType, WeeklyMission> missions = findOrCreateWeeklyMissions(user, weekStartDate);
        WeeklyMission weeklyMission = missions.get(missionType);
        if (weeklyMission == null) {
            throw new BusinessException(ErrorCode.MISSION_NOT_FOUND);
        }

        if (!weeklyMission.isAchieved()) {
            throw new BusinessException(ErrorCode.MISSION_NOT_ACHIEVED);
        }
        if (weeklyMission.isClaimed()) {
            throw new BusinessException(ErrorCode.MISSION_ALREADY_CLAIMED);
        }

        weeklyMission.claim(LocalDateTime.now());
        user.gainExp(EXP_PER_MISSION);

        return toSummary(user, weekStartDate, missions);
    }

    private Map<MissionType, WeeklyMission> findOrCreateWeeklyMissions(User user, LocalDate weekStartDate){
        Map<MissionType, WeeklyMission> existingMissions = weeklyMissionRepository.findByUserIdAndWeekStartDate(user.getId(), weekStartDate).stream()
                .collect(Collectors.toMap(WeeklyMission::getMissionType, Function.identity()));

        for (MissionType missionType : MissionType.values()) {
            existingMissions.computeIfAbsent(missionType,
                    type -> weeklyMissionRepository.save(WeeklyMission.createFor(user, type, weekStartDate)));
        }
        return existingMissions;
    }

    private WeeklyMissionSummaryResponse toSummary(User user, LocalDate weekStartDate, Map<MissionType, WeeklyMission> missions){
        List<MissionResponse> missionResponses = List.of(MissionType.values()).stream()
                .map(missions::get)
                .map(MissionResponse::from)
                .toList();

        int gaugePoints = (int) missionResponses.stream().filter(MissionResponse::achieved).count() * MissionType.POINTS_PER_MISSION;

        return new WeeklyMissionSummaryResponse(
                weekStartDate,
                gaugePoints,
                MissionType.WEEKLY_TOTAL_POINTS,
                user.getLevel(),
                user.getExperience(),
                missionResponses
        );
    }

    private LocalDate currentWeekStart(){
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
