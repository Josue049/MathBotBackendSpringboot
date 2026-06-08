package com.mathbot.backend.models.dto.chat;

import java.util.List;

public record ChatConversation(
        Long id,
        String title,
        String created_at,
        List<ChatMessage> messages) {
}
