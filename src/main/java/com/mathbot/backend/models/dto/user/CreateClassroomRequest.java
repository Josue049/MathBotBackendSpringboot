package com.mathbot.backend.models.dto.user;

import jakarta.validation.constraints.NotBlank;

public record CreateClassroomRequest(
        @NotBlank String name,
        String grade) {
}
