package com.mathbot.backend.models.dto.chat;

public record ChatStartResponse(boolean ok, Long conversationId, String title) {
}
