package com.example.app.domain.auth.dto;

import com.example.app.domain.auth.entity.User;

public record UserDto(
        String id,
        String email,
        String name,
        String role,
        String avatarUrl
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getAvatarUrl()
        );
    }
}
