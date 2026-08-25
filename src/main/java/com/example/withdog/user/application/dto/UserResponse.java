package com.example.withdog.user.application.dto;

import com.example.withdog.user.domain.User;

public record UserResponse(Long id, String nickName, String name, String email, String region, String address) implements UserProfileResponse {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(),user.getNickname(), user.getName(), user.getEmail(), user.getRegion(), user.getAddress());
    }
}
