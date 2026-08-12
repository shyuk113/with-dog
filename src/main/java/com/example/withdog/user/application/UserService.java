package com.example.withdog.user.application;

import com.example.withdog.user.application.dto.UpdateUserProfileRequest;
import com.example.withdog.user.application.dto.UserResponse;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    //유저 정보 상세 조회
    @Transactional(readOnly = true)
    public UserResponse getUserDetails(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));
        return UserResponse.from(user);
    }

    //유저 프로필 수정
    @Transactional
    public void updateUserProfile(UpdateUserProfileRequest request, Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));
        user.updateUserProfile(request.nickname(), request.region());
    }

}
