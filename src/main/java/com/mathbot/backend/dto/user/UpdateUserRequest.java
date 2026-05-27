package com.mathbot.backend.dto.user;

public record UpdateUserRequest(
                String nombre,
                String grado,
                String avatar) {
}
