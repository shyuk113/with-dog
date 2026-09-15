package com.example.withdog.dog.application;

import com.example.withdog.dog.application.dto.DogResponse;
import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.constant.DefaultImages;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class DogServiceTest {

    @Autowired
    private DogService dogService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DogRepository dogRepository;

    private Long userId;
    private Long otherUserId;
    private Long dogId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"));
        User other = userRepository.save(User.createUserLocal("타인", "other@example.com", "encoded-password", "부산"));
        Dog dog = dogRepository.save(Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 4.2, user));
        userId = user.getId();
        otherUserId = other.getId();
        dogId = dog.getId();
    }

    @Test
    void 프로필_이미지가_없으면_기본_강아지_이미지가_응답된다() {
        DogResponse response = dogService.getDog(dogId, userId);

        assertThat(response.imageUrl()).isEqualTo(DefaultImages.DOG_PROFILE);
    }

    @Test
    void 강아지_프로필_이미지를_업로드할_수_있다() {
        MockMultipartFile image = new MockMultipartFile("image", "dog.jpg", "image/jpeg", "fake-bytes".getBytes());

        DogResponse response = dogService.updateProfileImage(dogId, userId, image);

        assertThat(response.imageUrl()).startsWith("/uploads/dogs/");
        assertThat(response.imageUrl()).isNotEqualTo(DefaultImages.DOG_PROFILE);
    }

    @Test
    void 타인의_강아지에는_이미지를_업로드할_수_없다() {
        MockMultipartFile image = new MockMultipartFile("image", "dog.jpg", "image/jpeg", "fake-bytes".getBytes());

        assertThatThrownBy(() -> dogService.updateProfileImage(dogId, otherUserId, image))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DOG_NOT_FOUND);
    }
}
