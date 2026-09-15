package com.example.withdog.dog.domain;

import com.example.withdog.global.BaseEntity;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Breed breed;

    private LocalDate birthDate;

    private Double weight;

    private String imageUrl;

    // 급여량 추천 등에 활용되는 지병/건강상태 (예: 비만, 저체중, 신장질환)
    @ElementCollection
    @CollectionTable(name = "dog_health_condition", joinColumns = @JoinColumn(name = "dog_id"))
    @Column(name = "condition")
    private List<String> healthConditions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private Dog(String name, Breed breed, LocalDate birthDate, Double weight, User user) {
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.weight = weight;
        this.user = user;
    }

    public static Dog createDogProfile(String name, Breed breed, LocalDate birthDate, Double weight, User user) {
        return Dog.builder()
                .name(name)
                .breed(breed)
                .birthDate(birthDate)
                .weight(weight)
                .user(user)
                .build();
    }

    public int getAge() {
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }

    public void updateDogProfile(String name, Breed breed, LocalDate birthDate, Double weight) {
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.weight = weight;
    }

    public void updateHealthConditions(List<String> healthConditions) {
        this.healthConditions = new ArrayList<>(healthConditions);
    }

    public void updateProfileImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
