package com.example.withdog.auth.application.dto;

public record SignupRequest(String name, String email, String password, String region) {
}
