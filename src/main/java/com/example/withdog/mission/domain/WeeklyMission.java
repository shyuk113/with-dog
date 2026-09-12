package com.example.withdog.mission.domain;

import com.example.withdog.global.BaseEntity;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start_date", "mission_type"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private MissionType missionType;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    // 달성 여부 — 조건 충족 시 게이지가 차오르는 시점 (경험치 지급과 무관)
    private boolean achieved;

    private LocalDateTime achievedAt;

    // 수령 여부 — 유저가 완료 버튼을 눌러 경험치를 실제로 받은 시점
    private boolean claimed;

    private LocalDateTime claimedAt;

    @Builder
    private WeeklyMission(User user, MissionType missionType, LocalDate weekStartDate) {
        this.user = user;
        this.missionType = missionType;
        this.weekStartDate = weekStartDate;
        this.achieved = false;
        this.claimed = false;
    }

    public static WeeklyMission createFor(User user, MissionType missionType, LocalDate weekStartDate) {
        return WeeklyMission.builder()
                .user(user)
                .missionType(missionType)
                .weekStartDate(weekStartDate)
                .build();
    }

    public void achieve(LocalDateTime achievedAt) {
        this.achieved = true;
        this.achievedAt = achievedAt;
    }

    public void claim(LocalDateTime claimedAt) {
        this.claimed = true;
        this.claimedAt = claimedAt;
    }
}
