package com.example.withdog.user.application.dto;

import com.example.withdog.user.domain.User;

public record UserPublicResponse(Long id, String nickName, String region) implements UserProfileResponse {

    public static UserPublicResponse from(User user) {
        return new UserPublicResponse(user.getId(), user.getNickname(), user.getRegion());
    }
}
