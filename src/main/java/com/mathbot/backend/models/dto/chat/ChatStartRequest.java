package com.mathbot.backend.models.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatStartRequest(
        @NotNull Long userId,
        @NotBlank String firstMessage) {
}
