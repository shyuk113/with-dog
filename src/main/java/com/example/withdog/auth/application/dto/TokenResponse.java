package com.example.withdog.auth.application.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn, Boolean isNewUser) {
}
