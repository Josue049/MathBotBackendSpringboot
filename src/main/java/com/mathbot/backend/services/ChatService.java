package com.mathbot.backend.services;

import com.mathbot.backend.models.dto.chat.ChatConversation;
import com.mathbot.backend.models.dto.chat.ChatConversationResponse;
import com.mathbot.backend.models.dto.chat.ChatHistoryItem;
import com.mathbot.backend.models.dto.chat.ChatHistoryResponse;
import com.mathbot.backend.models.dto.chat.ChatMessage;
import com.mathbot.backend.models.dto.chat.ChatRequest;
import com.mathbot.backend.models.dto.chat.ChatResponse;
import com.mathbot.backend.models.dto.chat.ChatStartRequest;
import com.mathbot.backend.models.dto.chat.ChatStartResponse;
import com.mathbot.backend.models.entity.Conversation;
import com.mathbot.backend.models.entity.ConversationType;
import com.mathbot.backend.models.entity.Message;
import com.mathbot.backend.integrations.GroqChatService;
import com.mathbot.backend.repositories.ConversationRepository;
import com.mathbot.backend.repositories.MessageRepository;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GroqChatService groqChatService;
    private final AuthAccessService authAccessService;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GroqChatService groqChatService,
            AuthAccessService authAccessService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.groqChatService = groqChatService;
        this.authAccessService = authAccessService;
    }

    @Transactional
    public ChatStartResponse start(String username, ChatStartRequest request) {
        authAccessService.assertCanAccessUserData(username, request.userId());

        String first = request.firstMessage().trim();
        if (first.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El primer mensaje es obligatorio");
        }

        try {
            String title = groqChatService.generateMathTitle(first);
            Conversation conversation = new Conversation();
            conversation.setUserId(request.userId());
            conversation.setTitle(title);
            conversation.setType(ConversationType.TEXT);
            Conversation saved = conversationRepository.save(conversation);
            return new ChatStartResponse(true, saved.getId(), title);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Transactional
    public ChatResponse chat(String username, ChatRequest request) {
        authAccessService.assertCanAccessUserData(username, request.userId());

        String text = request.message().trim();
        if (text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje es obligatorio");
        }

        Conversation conversation = conversationRepository
                .findByIdAndUserId(request.conversationId(), request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));

        try {
            List<Map<String, String>> memory = buildMemoryMessages(conversation.getId());
            memory.add(Map.of("role", "user", "content", text));

            String reply = groqChatService.complete(memory, null);
            if (reply == null || reply.isBlank()) {
                reply = "Lo siento, no pude responder ahora.";
            }

            saveMessage(conversation.getId(), "user", text);
            saveMessage(conversation.getId(), "assistant", reply);
            touchConversation(conversation);

            return new ChatResponse(true, reply, conversation.getId());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    public ChatHistoryResponse history(String username, Long userId, int limit) {
        authAccessService.assertCanAccessUserData(username, userId);

        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Conversation> rows = conversationRepository.findByUserIdOrderByUpdatedAtDesc(
                userId, PageRequest.of(0, safeLimit));

        List<ChatHistoryItem> items = rows.stream()
                .map(row -> new ChatHistoryItem(
                        row.getId(),
                        row.getTitle(),
                        formatIso(row.getCreatedAt()),
                        formatIso(row.getUpdatedAt())))
                .toList();

        return new ChatHistoryResponse(true, items);
    }

    public ChatConversationResponse conversation(String username, Long userId, Long conversationId) {
        authAccessService.assertCanAccessUserData(username, userId);

        Conversation convo = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));

        List<Message> rows = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<ChatMessage> messages = rows.stream()
                .map(row -> new ChatMessage(row.getRole(), row.getContent()))
                .toList();

        ChatConversation payload = new ChatConversation(
                convo.getId(),
                convo.getTitle(),
                formatIso(convo.getCreatedAt()),
                messages);

        return new ChatConversationResponse(true, payload);
    }

    public long countConversationsForUser(Long userId) {
        return conversationRepository.countByUserId(userId);
    }

    public String lastActivityForUser(Long userId) {
        return conversationRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .map(conversation -> formatIso(conversation.getUpdatedAt()))
                .orElse(null);
    }

    private List<Map<String, String>> buildMemoryMessages(Long conversationId) {
        List<Map<String, String>> memory = new ArrayList<>();
        memory.add(Map.of("role", "system", "content", groqChatService.tutorSystemPrompt()));

        List<Message> rows = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        for (Message row : rows) {
            memory.add(Map.of("role", row.getRole(), "content", row.getContent()));
        }
        return memory;
    }

    private void saveMessage(Long conversationId, String role, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        messageRepository.save(message);
    }

    private void touchConversation(Conversation conversation) {
        conversationRepository.save(conversation);
    }

    private String formatIso(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(ISO_FORMAT);
    }
}
