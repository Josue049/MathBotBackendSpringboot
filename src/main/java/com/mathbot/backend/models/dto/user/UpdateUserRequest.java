package com.mathbot.backend.models.dto.user;

public record UpdateUserRequest(
                String nombre,
                String grado,
                String avatar) {
}
