package com.example.withdog.user.application.dto;

import com.example.withdog.global.constant.DefaultImages;
import com.example.withdog.user.domain.User;

public record UserPublicResponse(Long id, String nickName, String region, String imageUrl) implements UserProfileResponse {

    public static UserPublicResponse from(User user) {
        String imageUrl = user.getImageUrl() != null ? user.getImageUrl() : DefaultImages.USER_PROFILE;
        return new UserPublicResponse(user.getId(), user.getNickname(), user.getRegion(), imageUrl);
    }
}
