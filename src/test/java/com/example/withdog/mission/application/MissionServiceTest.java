package com.example.withdog.mission.application;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.mission.application.dto.MissionResponse;
import com.example.withdog.mission.application.dto.WeeklyMissionSummaryResponse;
import com.example.withdog.mission.domain.MissionType;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.walk.domain.Walk;
import com.example.withdog.walk.infrastructure.WalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class MissionServiceTest {

    @Autowired
    private MissionService missionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DogRepository dogRepository;

    @Autowired
    private WalkRepository walkRepository;

    private Long userId;
    private Long dogId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"));
        Dog dog = dogRepository.save(Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 4.2, user));
        userId = user.getId();
        dogId = dog.getId();
    }

    private void completeWalk(LocalDateTime startedAt, LocalDateTime endedAt, double distanceKm) {
        User user = userRepository.findById(userId).orElseThrow();
        Dog dog = dogRepository.findById(dogId).orElseThrow();
        Walk walk = Walk.createWalk(user, dog, startedAt);
        walk.end(endedAt, distanceKm);
        walkRepository.save(walk);
    }

    private Map<MissionType, MissionResponse> byType(WeeklyMissionSummaryResponse summary) {
        return summary.missions().stream().collect(Collectors.toMap(MissionResponse::missionType, Function.identity()));
    }

    @Test
    void 아직_산책기록이_없으면_5개_미션_모두_미달성_상태로_노출된다() {
        WeeklyMissionSummaryResponse summary = missionService.getWeeklyMissions(userId);

        assertThat(summary.missions()).hasSize(5);
        assertThat(summary.missions()).allMatch(m -> !m.achieved() && !m.claimed());
        assertThat(summary.gaugePoints()).isEqualTo(0);
        assertThat(summary.totalPoints()).isEqualTo(100);
        assertThat(summary.level()).isEqualTo(1);
        assertThat(summary.experience()).isEqualTo(0);
    }

    @Test
    void 조회는_재계산을_하지_않고_저장된_상태만_반환한다() {
        // 산책 기록은 있지만 evaluate()를 호출하지 않았으므로 게이지가 그대로 0이어야 한다
        completeWalk(LocalDateTime.now().minusMinutes(65), LocalDateTime.now(), 6.0);

        WeeklyMissionSummaryResponse summary = missionService.getWeeklyMissions(userId);

        assertThat(summary.gaugePoints()).isEqualTo(0);
        assertThat(summary.missions()).allMatch(m -> !m.achieved());
    }

    @Test
    void 산책_1회로_거리와_시간_조건을_채우면_해당_미션이_달성되지만_경험치는_아직_지급되지_않는다() {
        LocalDateTime now = LocalDateTime.now();
        completeWalk(now.minusMinutes(65), now, 6.0); // 65분, 6km -> WALK_ONCE, WALK_DISTANCE_5KM, WALK_DURATION_60MIN 달성

        missionService.evaluate(userId);
        WeeklyMissionSummaryResponse summary = missionService.getWeeklyMissions(userId);
        Map<MissionType, MissionResponse> missions = byType(summary);

        assertThat(missions.get(MissionType.WALK_ONCE).achieved()).isTrue();
        assertThat(missions.get(MissionType.WALK_DISTANCE_5KM).achieved()).isTrue();
        assertThat(missions.get(MissionType.WALK_DURATION_60MIN).achieved()).isTrue();
        assertThat(missions.get(MissionType.WALK_THREE_TIMES).achieved()).isFalse();
        assertThat(missions.get(MissionType.WALK_FIVE_TIMES).achieved()).isFalse();

        // 게이지는 달성 기준으로 차오르지만
        assertThat(summary.gaugePoints()).isEqualTo(60);
        // 완료 버튼을 누르기 전이라 경험치/레벨은 아직 그대로
        assertThat(summary.experience()).isEqualTo(0);
        assertThat(summary.level()).isEqualTo(1);
        assertThat(missions.get(MissionType.WALK_ONCE).claimed()).isFalse();
    }

    @Test
    void 달성한_미션을_완료버튼으로_수령하면_그때_경험치를_획득한다() {
        LocalDateTime now = LocalDateTime.now();
        completeWalk(now.minusMinutes(65), now, 6.0);
        missionService.evaluate(userId);

        WeeklyMissionSummaryResponse afterClaim = missionService.claimMission(userId, MissionType.WALK_ONCE);

        Map<MissionType, MissionResponse> missions = byType(afterClaim);
        assertThat(missions.get(MissionType.WALK_ONCE).claimed()).isTrue();
        assertThat(afterClaim.experience()).isEqualTo(20);

        // 아직 수령하지 않은 다른 달성 미션들의 경험치는 반영되지 않음
        assertThat(missions.get(MissionType.WALK_DISTANCE_5KM).claimed()).isFalse();
    }

    @Test
    void 달성하지_않은_미션은_완료버튼을_눌러도_수령할_수_없다() {
        assertThatThrownBy(() -> missionService.claimMission(userId, MissionType.WALK_FIVE_TIMES))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISSION_NOT_ACHIEVED);
    }

    @Test
    void 이미_수령한_미션은_다시_수령할_수_없다() {
        LocalDateTime now = LocalDateTime.now();
        completeWalk(now.minusMinutes(65), now, 6.0);
        missionService.evaluate(userId);
        missionService.claimMission(userId, MissionType.WALK_ONCE);

        assertThatThrownBy(() -> missionService.claimMission(userId, MissionType.WALK_ONCE))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISSION_ALREADY_CLAIMED);
    }

    @Test
    void 다섯개_미션을_모두_달성하고_전부_수령하면_100_경험치로_레벨업한다() {
        LocalDateTime now = LocalDateTime.now();
        completeWalk(now.minusMinutes(20), now.minusMinutes(19), 1.2);
        completeWalk(now.minusMinutes(18), now.minusMinutes(17), 1.2);
        completeWalk(now.minusMinutes(16), now.minusMinutes(15), 1.2);
        completeWalk(now.minusMinutes(14), now.minusMinutes(13), 1.2);
        completeWalk(now.minusMinutes(70), now, 1.2);

        missionService.evaluate(userId);

        WeeklyMissionSummaryResponse result = null;
        for (MissionType missionType : MissionType.values()) {
            result = missionService.claimMission(userId, missionType);
        }

        assertThat(result.missions()).allMatch(m -> m.achieved() && m.claimed());
        // 미션 5개 * 20 경험치 = 100, 레벨1의 다음 레벨업 요구치가 100이므로 정확히 레벨업
        assertThat(result.level()).isEqualTo(2);
        assertThat(result.experience()).isEqualTo(0);
    }
}
