package com.example.withdog.user.application.dto;

import com.example.withdog.user.domain.User;

public record UserResponse(Long id, String name, String email, String region, String address) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRegion(), user.getAddress());
    }
}
