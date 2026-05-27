package com.mathbot.backend.controller;

import com.mathbot.backend.dto.chat.ChatHistoryResponse;
import com.mathbot.backend.dto.chat.ChatConversationResponse;
import com.mathbot.backend.dto.chat.ChatRequest;
import com.mathbot.backend.dto.chat.ChatResponse;
import com.mathbot.backend.dto.chat.ChatStartRequest;
import com.mathbot.backend.dto.chat.ChatStartResponse;
import com.mathbot.backend.service.FastApiAiClient;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final FastApiAiClient fastApiAiClient;

    public ChatController(FastApiAiClient fastApiAiClient) {
        this.fastApiAiClient = fastApiAiClient;
    }

    @PostMapping("/chat/start")
    public ChatStartResponse start(@Valid @RequestBody @NonNull ChatStartRequest request) {
        return fastApiAiClient.start(request);
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody @NonNull ChatRequest request) {
        return fastApiAiClient.send(request);
    }

    @GetMapping("/history/{userId}")
    public ChatHistoryResponse history(@PathVariable Long userId,
            @RequestParam(defaultValue = "6") int limit) {
        return fastApiAiClient.history(userId, limit);
    }

    @GetMapping("/history/{userId}/{conversationId}")
    public ChatConversationResponse conversation(@PathVariable Long userId,
            @PathVariable Long conversationId) {
        return fastApiAiClient.conversation(userId, conversationId);
    }
}