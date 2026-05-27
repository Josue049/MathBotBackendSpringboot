package com.mathbot.backend.dto.auth;

import com.mathbot.backend.dto.user.UserResponse;

public record AuthResponse(
        boolean ok,
        String message,
        String token,
        UserResponse user) {
}