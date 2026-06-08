package com.mathbot.backend.models.dto.chat;

public record ChatHistoryItem(
        Long id,
        String title,
        String created_at,
        String updated_at) {
}
