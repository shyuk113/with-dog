package com.example.withdog.walk.application;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.walk.application.dto.CreateWalkRequest;
import com.example.withdog.walk.application.dto.RoutePointRequest;
import com.example.withdog.walk.application.dto.UpdateWalkRequest;
import com.example.withdog.walk.application.dto.WalkDetailResponse;
import com.example.withdog.walk.application.dto.WalkResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class WalkServiceTest {

    @Autowired
    private WalkService walkService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DogRepository dogRepository;

    private Long userId;
    private Long dogId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"));
        Dog dog = dogRepository.save(Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 4.2, user));
        userId = user.getId();
        dogId = dog.getId();
    }

    @Test
    void 산책_종료시_강아지_프로필과_코스가_포함된_상세정보를_바로_반환한다() {
        WalkResponse started = walkService.createWalk(userId, new CreateWalkRequest(dogId, LocalDateTime.now()));

        List<RoutePointRequest> routePoints = List.of(
                new RoutePointRequest(37.5665, 126.9780, LocalDateTime.now().minusMinutes(10)),
                new RoutePointRequest(37.5670, 126.9790, LocalDateTime.now())
        );
        UpdateWalkRequest request = new UpdateWalkRequest(1.2, routePoints);

        WalkDetailResponse detail = walkService.updateWalk(userId, started.id(), request);

        assertThat(detail.id()).isEqualTo(started.id());
        assertThat(detail.dog().id()).isEqualTo(dogId);
        assertThat(detail.dog().name()).isEqualTo("콩이");
        assertThat(detail.distanceKm()).isEqualTo(1.2);
        assertThat(detail.endedAt()).isNotNull();
        assertThat(detail.routePoints()).hasSize(2);
    }

    @Test
    void 히스토리_상세조회는_저장된_코스와_강아지_프로필을_함께_반환한다() {
        WalkResponse started = walkService.createWalk(userId, new CreateWalkRequest(dogId, LocalDateTime.now()));
        walkService.updateWalk(userId, started.id(), new UpdateWalkRequest(2.5, List.of(
                new RoutePointRequest(37.0, 127.0, LocalDateTime.now())
        )));

        WalkDetailResponse detail = walkService.getWalkHistory(started.id(), userId);

        assertThat(detail.dog().breed()).isEqualTo(Breed.POODLE);
        assertThat(detail.routePoints()).hasSize(1);
        assertThat(detail.distanceKm()).isEqualTo(2.5);
    }

    @Test
    void 목록조회는_기존과_같이_가벼운_응답을_반환한다() {
        WalkResponse started = walkService.createWalk(userId, new CreateWalkRequest(dogId, LocalDateTime.now()));
        walkService.updateWalk(userId, started.id(), new UpdateWalkRequest(3.0, List.of()));

        var page = walkService.getWalkHistories(PageRequest.of(0, 10), userId);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).dogId()).isEqualTo(dogId);
    }
}
