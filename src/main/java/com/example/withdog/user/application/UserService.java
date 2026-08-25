package com.example.withdog.user.application;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.user.application.dto.UpdateUserProfileRequest;
import com.example.withdog.user.application.dto.UserProfileResponse;
import com.example.withdog.user.application.dto.UserPublicResponse;
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
    public UserProfileResponse getUserDetails(Long targetId, Long userId){
        User user = userRepository.findById(targetId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if(targetId.equals(userId)){
            return UserResponse.from(user);
        }
        return UserPublicResponse.from(user);
    }

    //유저 프로필 수정
    @Transactional
    public void updateUserProfile(UpdateUserProfileRequest request, Long targetId,Long userId){
        if(!targetId.equals(userId)){
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        User user = userRepository.findById(targetId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateUserProfile(request.nickname(), request.region(), request.latitude(), request.longitude(), request.address());
    }

}
