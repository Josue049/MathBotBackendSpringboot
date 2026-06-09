package com.mathbot.backend.models.dto.chat;

public record ChatVisionResponse(
        boolean ok,
        Long conversationId,
        String reply,
        String imageUrl) {
}
