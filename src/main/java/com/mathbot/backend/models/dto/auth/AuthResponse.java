package com.mathbot.backend.models.dto.auth;

import com.mathbot.backend.models.dto.user.UserResponse;

public record AuthResponse(
        boolean ok,
        String message,
        String token,
        UserResponse user) {
}