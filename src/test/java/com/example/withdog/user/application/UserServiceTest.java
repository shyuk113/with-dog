package com.example.withdog.user.application;

import com.example.withdog.global.constant.DefaultImages;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.user.application.dto.UserProfileResponse;
import com.example.withdog.user.application.dto.UserResponse;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"));
        User other = userRepository.save(User.createUserLocal("타인", "other@example.com", "encoded-password", "부산"));
        userId = user.getId();
        otherUserId = other.getId();
    }

    @Test
    void 프로필_이미지를_등록하지_않으면_기본_이미지가_응답된다() {
        UserProfileResponse response = userService.getUserDetails(userId, userId);

        assertThat(response).isInstanceOf(UserResponse.class);
        assertThat(((UserResponse) response).imageUrl()).isEqualTo(DefaultImages.USER_PROFILE);
    }

    @Test
    void 본인_프로필_이미지를_업로드할_수_있다() {
        MockMultipartFile image = new MockMultipartFile("image", "me.jpg", "image/jpeg", "fake-bytes".getBytes());

        UserResponse response = userService.updateProfileImage(userId, userId, image);

        assertThat(response.imageUrl()).startsWith("/uploads/users/");
        assertThat(response.imageUrl()).isNotEqualTo(DefaultImages.USER_PROFILE);
    }

    @Test
    void 타인의_프로필_이미지는_업로드할_수_없다() {
        MockMultipartFile image = new MockMultipartFile("image", "me.jpg", "image/jpeg", "fake-bytes".getBytes());

        assertThatThrownBy(() -> userService.updateProfileImage(userId, otherUserId, image))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_ACCESS);
    }
}
