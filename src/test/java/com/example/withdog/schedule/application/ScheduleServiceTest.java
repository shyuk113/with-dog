package com.example.withdog.schedule.application;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.schedule.application.dto.request.CreateScheduleRequest;
import com.example.withdog.schedule.application.dto.request.UpdateScheduleRequest;
import com.example.withdog.schedule.application.dto.request.UpdateStatus;
import com.example.withdog.schedule.application.dto.response.ScheduleResponse;
import com.example.withdog.schedule.domain.ScheduleStatus;
import com.example.withdog.schedule.domain.ScheduleType;
import com.example.withdog.schedule.infrastructure.ScheduleRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class ScheduleServiceTest {

    @Autowired private ScheduleService scheduleService;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DogRepository dogRepository;
    @Autowired private EntityManager entityManager;

    private Long userId;
    private Long otherUserId;
    private Long dogId;
    private Long otherDogId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "schedule@example.com", "encoded-password", "서울"));
        User other = userRepository.save(User.createUserLocal("타인", "schedule-other@example.com", "encoded-password", "부산"));
        userId = user.getId();
        otherUserId = other.getId();
        dogId = dogRepository.save(Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 4.2, user)).getId();
        otherDogId = dogRepository.save(Dog.createDogProfile("초코", Breed.BEAGLE, LocalDate.of(2020, 1, 1), 8.0, other)).getId();
    }

    private CreateScheduleRequest grooming(LocalDateTime startAt) {
        return new CreateScheduleRequest(dogId, ScheduleType.GROOMING, "전체 미용", "멍멍 애견미용", "얼굴컷 짧게",
                startAt, startAt.plusHours(2), null);
    }

    private ScheduleResponse create(ScheduleType type, Long dog, LocalDateTime startAt, LocalDateTime endAt) {
        return scheduleService.createSchedule(
                new CreateScheduleRequest(dog, type, type.name(), null, null, startAt, endAt, false), userId);
    }

    @Test
    void 미용_예약을_등록하면_예정_상태로_저장된다() {
        LocalDateTime startAt = LocalDateTime.of(2026, 11, 20, 14, 0);

        ScheduleResponse response = scheduleService.createSchedule(grooming(startAt), userId);

        assertThat(response.id()).isNotNull();
        assertThat(response.dogId()).isEqualTo(dogId);
        assertThat(response.type()).isEqualTo(ScheduleType.GROOMING);
        assertThat(response.status()).isEqualTo(ScheduleStatus.SCHEDULED);
        assertThat(response.placeName()).isEqualTo("멍멍 애견미용");
        assertThat(response.endAt()).isEqualTo(startAt.plusHours(2));
        assertThat(response.allDay()).isFalse(); // allDay를 보내지 않으면 false
    }

    @Test
    void 강아지를_지정하지_않은_일정도_등록할_수_있다() {
        ScheduleResponse response = create(ScheduleType.HOSPITAL, null, LocalDateTime.of(2026, 11, 21, 10, 0), null);

        assertThat(response.dogId()).isNull();
        assertThat(response.endAt()).isNull();
    }

    @Test
    void 다른_사람의_강아지로는_일정을_등록할_수_없다() {
        assertThatThrownBy(() -> create(ScheduleType.HOSPITAL, otherDogId, LocalDateTime.of(2026, 11, 21, 10, 0), null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DOG_NOT_FOUND);
    }

    @Test
    void 종료_시간이_시작_시간보다_빠르면_등록할_수_없다() {
        LocalDateTime startAt = LocalDateTime.of(2026, 11, 20, 14, 0);

        assertThatThrownBy(() -> create(ScheduleType.GROOMING, dogId, startAt, startAt.minusMinutes(1)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SCHEDULE_TIME);
    }

    @Test
    void 월간_조회는_기간과_겹치는_일정만_시작_시간순으로_돌려준다() {
        Long crossing = create(ScheduleType.HOSPITAL, dogId, LocalDateTime.of(2026, 10, 31, 22, 0), LocalDateTime.of(2026, 11, 1, 9, 0)).id();
        Long last = create(ScheduleType.GROOMING, dogId, LocalDateTime.of(2026, 11, 30, 23, 0), null).id();
        Long middle = create(ScheduleType.VACCINATION, dogId, LocalDateTime.of(2026, 11, 15, 10, 0), null).id();
        create(ScheduleType.HOSPITAL, dogId, LocalDateTime.of(2026, 10, 20, 10, 0), null); // 10월
        create(ScheduleType.HOSPITAL, dogId, LocalDateTime.of(2026, 12, 1, 0, 0), null);   // 12월 1일 0시
        //11/1 0시 정각에 끝나는 일정은 11월에 걸치지 않는다 (종료 시각은 포함하지 않음)
        create(ScheduleType.HOSPITAL, dogId, LocalDateTime.of(2026, 10, 31, 22, 0), LocalDateTime.of(2026, 11, 1, 0, 0));

        List<ScheduleResponse> november = scheduleService.getSchedules(userId,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null, null);

        assertThat(november).extracting(ScheduleResponse::id).containsExactly(crossing, middle, last);
    }

    @Test
    void 강아지와_종류로_걸러서_조회할_수_있다() {
        Dog second = dogRepository.save(Dog.createDogProfile("두부", Breed.BEAGLE, LocalDate.of(2022, 5, 5), 9.0,
                userRepository.findById(userId).orElseThrow()));
        Long kongGrooming = create(ScheduleType.GROOMING, dogId, LocalDateTime.of(2026, 11, 3, 10, 0), null).id();
        create(ScheduleType.HOSPITAL, dogId, LocalDateTime.of(2026, 11, 4, 10, 0), null);
        create(ScheduleType.GROOMING, second.getId(), LocalDateTime.of(2026, 11, 5, 10, 0), null);

        List<ScheduleResponse> filtered = scheduleService.getSchedules(userId,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), dogId, ScheduleType.GROOMING);

        assertThat(filtered).extracting(ScheduleResponse::id).containsExactly(kongGrooming);
    }

    @Test
    void 다른_사람의_일정은_조회되지_않는다() {
        scheduleService.createSchedule(new CreateScheduleRequest(otherDogId, ScheduleType.GROOMING, "타인 미용", null, null,
                LocalDateTime.of(2026, 11, 10, 10, 0), null, false), otherUserId);

        assertThat(scheduleService.getSchedules(userId, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null, null))
                .isEmpty();
    }

    @Test
    void 조회_기간이_뒤집혔거나_62일을_넘으면_조회할_수_없다() {
        assertThatThrownBy(() -> scheduleService.getSchedules(userId, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SCHEDULE_PERIOD);
        assertThatThrownBy(() -> scheduleService.getSchedules(userId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SCHEDULE_PERIOD);
    }

    @Test
    void 다가오는_일정은_끝나지_않은_예정_일정만_가까운_순으로_보인다() {
        LocalDateTime now = LocalDateTime.now(ScheduleService.ZONE);
        Long soon = create(ScheduleType.GROOMING, dogId, now.plusDays(1), null).id();
        Long later = create(ScheduleType.HOSPITAL, dogId, now.plusDays(3), null).id();
        Long ongoing = create(ScheduleType.HOSPITAL, dogId, now.minusHours(1), now.plusHours(1)).id(); // 진행 중
        create(ScheduleType.HOSPITAL, dogId, now.minusDays(1), null);                                  // 지난 일정
        Long canceled = create(ScheduleType.VACCINATION, dogId, now.plusDays(2), null).id();
        scheduleService.updateScheduleStatus(new UpdateStatus(ScheduleStatus.CANCELED), canceled, userId);

        List<ScheduleResponse> upcoming = scheduleService.getUpcomingSchedules(userId, 5);

        assertThat(upcoming).extracting(ScheduleResponse::id).containsExactly(ongoing, soon, later);
    }

    @Test
    void 일정을_수정하면_강아지_연결도_바꿀_수_있다() {
        Long id = scheduleService.createSchedule(grooming(LocalDateTime.of(2026, 11, 20, 14, 0)), userId).id();
        LocalDateTime newStart = LocalDateTime.of(2026, 11, 22, 11, 0);

        ScheduleResponse updated = scheduleService.updateSchedule(new UpdateScheduleRequest(null, ScheduleType.HOSPITAL,
                "건강검진", "행복동물병원", null, newStart, null, true), id, userId);

        assertThat(updated.dogId()).isNull();
        assertThat(updated.type()).isEqualTo(ScheduleType.HOSPITAL);
        assertThat(updated.allDay()).isTrue();
        //종일로 바꾸면 그날 하루 전체로 정규화
        assertThat(updated.startAt()).isEqualTo(LocalDateTime.of(2026, 11, 22, 0, 0));
        assertThat(updated.endAt()).isEqualTo(LocalDateTime.of(2026, 11, 23, 0, 0));
    }

    private ScheduleResponse createAllDay(LocalDateTime startAt, LocalDateTime endAt) {
        return scheduleService.createSchedule(new CreateScheduleRequest(dogId, ScheduleType.VACCINATION, "종합 접종",
                null, null, startAt, endAt, true), userId);
    }

    @Test
    void 종일_일정은_그날_0시부터_다음날_0시까지로_저장된다() {
        ScheduleResponse response = createAllDay(LocalDateTime.of(2026, 11, 5, 15, 30), null);

        assertThat(response.startAt()).isEqualTo(LocalDateTime.of(2026, 11, 5, 0, 0));
        assertThat(response.endAt()).isEqualTo(LocalDateTime.of(2026, 11, 6, 0, 0));
    }

    @Test
    void 여러날_종일_일정은_마지막날_다음날_0시까지로_저장된다() {
        ScheduleResponse response = createAllDay(LocalDateTime.of(2026, 11, 5, 9, 0), LocalDateTime.of(2026, 11, 7, 8, 0));

        assertThat(response.startAt()).isEqualTo(LocalDateTime.of(2026, 11, 5, 0, 0));
        assertThat(response.endAt()).isEqualTo(LocalDateTime.of(2026, 11, 8, 0, 0));
    }

    @Test
    void 종일_일정은_같은_날이면_종료_시간이_시작보다_앞서도_되지만_전날로_끝날_수는_없다() {
        //날짜만 보므로 허용 (11/5 하루짜리)
        ScheduleResponse sameDay = createAllDay(LocalDateTime.of(2026, 11, 5, 15, 0), LocalDateTime.of(2026, 11, 5, 9, 0));
        assertThat(sameDay.endAt()).isEqualTo(LocalDateTime.of(2026, 11, 6, 0, 0));

        assertThatThrownBy(() -> createAllDay(LocalDateTime.of(2026, 11, 5, 9, 0), LocalDateTime.of(2026, 11, 4, 23, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SCHEDULE_TIME);
    }

    @Test
    void 종일_일정은_그날_조회에만_나오고_다음날_조회에는_나오지_않는다() {
        Long allDay = createAllDay(LocalDateTime.of(2026, 11, 5, 0, 0), null).id();

        assertThat(scheduleService.getSchedules(userId, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5), null, null))
                .extracting(ScheduleResponse::id).containsExactly(allDay);
        assertThat(scheduleService.getSchedules(userId, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6), null, null))
                .isEmpty();
        assertThat(scheduleService.getSchedules(userId, LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 4), null, null))
                .isEmpty();
    }

    @Test
    void 오늘_종일_일정은_0시가_지나도_다가오는_일정에_보인다() {
        LocalDateTime now = LocalDateTime.now(ScheduleService.ZONE);
        Long today = createAllDay(now, null).id();
        createAllDay(now.minusDays(1), null); // 어제 종일 일정은 끝남

        assertThat(scheduleService.getUpcomingSchedules(userId, 5))
                .extracting(ScheduleResponse::id).containsExactly(today);
    }

    @Test
    void 병원_다녀온_일정을_완료_처리할_수_있다() {
        Long id = create(ScheduleType.HOSPITAL, dogId, LocalDateTime.of(2026, 11, 2, 10, 0), null).id();

        ScheduleResponse done = scheduleService.updateScheduleStatus(new UpdateStatus(ScheduleStatus.DONE), id, userId);

        assertThat(done.status()).isEqualTo(ScheduleStatus.DONE);
    }

    @Test
    void 다른_사람의_일정은_조회_수정_삭제할_수_없다() {
        Long id = scheduleService.createSchedule(grooming(LocalDateTime.of(2026, 11, 20, 14, 0)), userId).id();

        assertThatThrownBy(() -> scheduleService.getSchedule(id, otherUserId))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_NOT_FOUND);
        assertThatThrownBy(() -> scheduleService.updateScheduleStatus(new UpdateStatus(ScheduleStatus.DONE), id, otherUserId))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_NOT_FOUND);
        assertThatThrownBy(() -> scheduleService.deleteSchedule(id, otherUserId))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    void 일정을_삭제할_수_있다() {
        Long id = scheduleService.createSchedule(grooming(LocalDateTime.of(2026, 11, 20, 14, 0)), userId).id();

        scheduleService.deleteSchedule(id, userId);

        assertThat(scheduleRepository.findById(id)).isEmpty();
    }

    @Test
    void 강아지_프로필을_삭제해도_일정은_남고_강아지_연결만_끊긴다() {
        Long id = scheduleService.createSchedule(grooming(LocalDateTime.of(2026, 11, 20, 14, 0)), userId).id();
        entityManager.flush();
        entityManager.clear();

        dogRepository.deleteById(dogId);
        entityManager.flush();
        entityManager.clear();

        ScheduleResponse remaining = scheduleService.getSchedule(id, userId);
        assertThat(remaining.dogId()).isNull();
        assertThat(remaining.title()).isEqualTo("전체 미용");
    }
}
