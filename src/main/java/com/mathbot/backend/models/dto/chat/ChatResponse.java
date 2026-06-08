package com.mathbot.backend.models.dto.chat;

public record ChatResponse(boolean ok, String reply, Long conversationId) {
}
