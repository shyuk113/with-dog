package com.example.withdog.user.application.dto;

public record UpdateUserProfileRequest(String nickname, String region, Double latitude, Double longitude, String address) {
}
