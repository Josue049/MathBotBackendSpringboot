package com.mathbot.backend.controllers;

import com.mathbot.backend.models.dto.chat.ChatConversationResponse;
import com.mathbot.backend.models.dto.chat.ChatHistoryResponse;
import com.mathbot.backend.models.dto.chat.ChatRequest;
import com.mathbot.backend.models.dto.chat.ChatResponse;
import com.mathbot.backend.models.dto.chat.ChatStartRequest;
import com.mathbot.backend.models.dto.chat.ChatStartResponse;
import com.mathbot.backend.services.ChatService;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat/start")
    public ChatStartResponse start(Authentication authentication, @Valid @RequestBody @NonNull ChatStartRequest request) {
        return chatService.start(authentication.getName(), request);
    }

    @PostMapping("/chat")
    public ChatResponse chat(Authentication authentication, @Valid @RequestBody @NonNull ChatRequest request) {
        return chatService.chat(authentication.getName(), request);
    }

    @GetMapping("/history/{userId}")
    public ChatHistoryResponse history(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") int limit) {
        return chatService.history(authentication.getName(), userId, limit);
    }

    @GetMapping("/history/{userId}/{conversationId}")
    public ChatConversationResponse conversation(
            Authentication authentication,
            @PathVariable Long userId,
            @PathVariable Long conversationId) {
        return chatService.conversation(authentication.getName(), userId, conversationId);
    }
}
