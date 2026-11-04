package com.example.withdog.schedule.domain;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.global.BaseEntity;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_schedule_user_start", columnList = "user_id, start_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    //강아지와 무관한 일정도 허용. 강아지 프로필을 삭제해도 일정 기록은 남기고 연결만 끊는다 (FK on delete set null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Dog dog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType scheduleType;

    @Column(nullable = false)
    private String title;
    private String placeName;
    private String memo;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus scheduleStatus;

    //예약 알림 (3단계에서 사용)
    private Integer remindBeforeMinute;

    private boolean reminderSent;

    @Builder
    private Schedule(User user, Dog dog, ScheduleType type, String title,
                     String placeName, String memo, LocalDateTime startAt,
                     LocalDateTime endAt, boolean allDay, ScheduleStatus status, Integer remindBeforeMinute, boolean reminderSent) {
        this.user = user;
        this.dog = dog;
        this.scheduleType = type;
        this.title = title;
        this.placeName = placeName;
        this.memo = memo;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allDay = allDay;
        this.scheduleStatus = status;
        this.remindBeforeMinute = remindBeforeMinute;
        this.reminderSent = reminderSent;
    }

    //새 일정은 항상 예정(SCHEDULED) 상태로 시작한다
    public static Schedule createSchedule(User user, Dog dog, ScheduleType type, String title, String placeName, String memo,
                                          LocalDateTime startAt, LocalDateTime endAt, boolean allDay) {
        Schedule schedule = Schedule.builder()
                .user(user)
                .dog(dog)
                .type(type)
                .title(title)
                .placeName(placeName)
                .memo(memo)
                .status(ScheduleStatus.SCHEDULED)
                .build();
        schedule.applyPeriod(startAt, endAt, allDay);
        return schedule;
    }

    public void updateSchedule(Dog dog, ScheduleType type, String title, String placeName, String memo,
                               LocalDateTime startAt, LocalDateTime endAt, boolean allDay){
        this.dog = dog;
        this.scheduleType = type;
        this.title = title;
        this.placeName = placeName;
        this.memo = memo;
        applyPeriod(startAt, endAt, allDay);
    }

    //종일 일정은 "시작일 0시 ~ 마지막 날 다음 날 0시"로 정규화해 저장한다 (종료 시각은 포함하지 않음)
    //  예) 11/5 종일 -> 11/5 00:00 ~ 11/6 00:00, 11/5~11/7 종일 -> 11/5 00:00 ~ 11/8 00:00
    //  종일 일정의 endAt은 시간과 상관없이 "마지막 날"로 해석하고, 없으면 시작일 하루짜리로 본다
    //이렇게 하면 캘린더 조회·다가오는 일정·알림이 모두 같은 [startAt, endAt) 기준으로 동작한다
    private void applyPeriod(LocalDateTime startAt, LocalDateTime endAt, boolean allDay) {
        this.allDay = allDay;
        if (!allDay) {
            this.startAt = startAt;
            this.endAt = endAt;
            return;
        }
        LocalDate firstDay = startAt.toLocalDate();
        LocalDate lastDay = endAt == null ? firstDay : endAt.toLocalDate();
        this.startAt = firstDay.atStartOfDay();
        this.endAt = lastDay.plusDays(1).atStartOfDay();
    }

    public void updateStatus(ScheduleStatus scheduleStatus){
        this.scheduleStatus = scheduleStatus;
    }

}
