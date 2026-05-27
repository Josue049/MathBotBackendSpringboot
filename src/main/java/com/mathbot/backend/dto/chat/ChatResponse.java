package com.mathbot.backend.dto.chat;

public record ChatResponse(boolean ok, String reply, Long conversationId) {
}