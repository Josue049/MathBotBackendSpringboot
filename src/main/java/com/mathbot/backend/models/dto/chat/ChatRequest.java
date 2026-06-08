package com.mathbot.backend.models.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotNull Long conversationId,
        @NotNull Long userId,
        @NotBlank String message) {
}