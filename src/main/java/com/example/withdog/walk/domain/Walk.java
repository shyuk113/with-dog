package com.example.withdog.walk.domain;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.global.BaseEntity;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Walk extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Double distanceKm;

    @Builder
    private Walk(User user, Dog dog, LocalDateTime startedAt) {
        this.user = user;
        this.dog = dog;
        this.startedAt = startedAt;
    }

    public static Walk createWalk(User user, Dog dog, LocalDateTime startedAt){
        return Walk.builder()
                .user(user)
                .dog(dog)
                .startedAt(startedAt)
                .build();
    }

    public void end(LocalDateTime endedAt, double distanceKm){
        this.endedAt = endedAt;
        this.distanceKm = distanceKm;
    }

    public boolean isOngoing(){
        return endedAt == null;
    }

    public long getDurationMinutes(){
        if (endedAt == null){
            return 0;
        }
        return Duration.between(startedAt,endedAt).toMinutes();
    }
}
