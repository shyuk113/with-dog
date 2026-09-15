package com.example.withdog.user.application.dto;

import com.example.withdog.global.constant.DefaultImages;
import com.example.withdog.user.domain.User;

public record UserResponse(Long id, String nickName, String name, String email, String region, String address, String imageUrl) implements UserProfileResponse {

    public static UserResponse from(User user) {
        String imageUrl = user.getImageUrl() != null ? user.getImageUrl() : DefaultImages.USER_PROFILE;
        return new UserResponse(user.getId(), user.getNickname(), user.getName(), user.getEmail(), user.getRegion(), user.getAddress(), imageUrl);
    }
}
