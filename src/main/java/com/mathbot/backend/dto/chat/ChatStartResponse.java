package com.mathbot.backend.dto.chat;

public record ChatStartResponse(boolean ok, Long conversationId, String title) {
}
