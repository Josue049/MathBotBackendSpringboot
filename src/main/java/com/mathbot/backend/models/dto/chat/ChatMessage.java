package com.mathbot.backend.models.dto.chat;

public record ChatMessage(String role, String content, String imageUrl) {
    public ChatMessage(String role, String content) {
        this(role, content, null);
    }
}
