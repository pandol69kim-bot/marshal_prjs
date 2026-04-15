package com.example.app.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
