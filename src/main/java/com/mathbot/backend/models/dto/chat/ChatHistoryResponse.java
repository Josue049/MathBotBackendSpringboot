package com.mathbot.backend.models.dto.chat;

import java.util.List;

public record ChatHistoryResponse(boolean ok, List<ChatHistoryItem> items) {
}
