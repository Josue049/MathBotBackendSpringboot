package com.mathbot.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathbot.backend.dto.chat.ChatHistoryResponse;
import com.mathbot.backend.dto.chat.ChatConversationResponse;
import com.mathbot.backend.dto.chat.ChatRequest;
import com.mathbot.backend.dto.chat.ChatResponse;
import com.mathbot.backend.dto.chat.ChatStartRequest;
import com.mathbot.backend.dto.chat.ChatStartResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class FastApiAiClient {

    private static final Logger log = LoggerFactory.getLogger(FastApiAiClient.class);

    private final RestTemplate restClient;
    private final String chatUrl;
    private final String startUrl;
    private final String historyUrl;

    private record FastApiStartRequest(Long user_id, String first_message) {
    }

    private record FastApiChatRequest(Long conversation_id, Long user_id, String message) {
    }

    public FastApiAiClient(
            @Value("${app.fastapi.chat-url:http://127.0.0.1:8000/api/chat}") String chatUrl,
            @Value("${app.fastapi.start-url:http://127.0.0.1:8000/api/chat/start}") String startUrl,
            @Value("${app.fastapi.history-url:http://127.0.0.1:8000/api/history}") String historyUrl) {
        this.chatUrl = normalizeBaseUrl(Objects.requireNonNull(chatUrl, "chatUrl"));
        this.startUrl = normalizeBaseUrl(Objects.requireNonNull(startUrl, "startUrl"));
        this.historyUrl = normalizeBaseUrl(Objects.requireNonNull(historyUrl, "historyUrl"));
        this.restClient = new RestTemplate();
    }

    private String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @SuppressWarnings("null")
    public ChatStartResponse start(@NonNull ChatStartRequest request) {
        FastApiStartRequest payload = new FastApiStartRequest(request.userId(), request.firstMessage());
        try {
            String json = new ObjectMapper().writeValueAsString(payload);
            log.debug("POST {} payload: {}", startUrl, json);
        } catch (JsonProcessingException ignored) {
        }

        HttpEntity<FastApiStartRequest> entity = new HttpEntity<>(payload, jsonHeaders());
        try {
            ChatStartResponse response = restClient.postForObject(startUrl, entity, ChatStartResponse.class);
            if (response == null) {
                return new ChatStartResponse(false, null, "No se pudo crear la conversación");
            }
            return response;
        } catch (HttpClientErrorException e) {
            return new ChatStartResponse(false, null, "Error FastAPI: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return new ChatStartResponse(false, null, "Error inesperado: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    public ChatResponse send(@NonNull ChatRequest request) {
        FastApiChatRequest payload = new FastApiChatRequest(
                request.conversationId(),
                request.userId(),
                request.message());
        try {
            String json = new ObjectMapper().writeValueAsString(payload);
            log.debug("POST {} payload: {}", chatUrl, json);
        } catch (JsonProcessingException ignored) {
        }
        HttpEntity<FastApiChatRequest> entity = new HttpEntity<>(payload, jsonHeaders());

        try {
            ChatResponse response = restClient.postForObject(chatUrl, entity, ChatResponse.class);
            if (response == null) {
                return new ChatResponse(false, "No se recibió respuesta de la IA", request.conversationId());
            }
            return response;
        } catch (HttpClientErrorException e) {
            return new ChatResponse(
                    false,
                    "Error al comunicarse con la IA: " + e.getResponseBodyAsString(),
                    request.conversationId());
        } catch (Exception e) {
            return new ChatResponse(false, "Error inesperado al llamar a la IA: " + e.getMessage(),
                    request.conversationId());
        }
    }

    @SuppressWarnings("null")
    public ChatHistoryResponse history(@NonNull Long userId, int limit) {
        String historyRequestUrl = historyUrl + "/" + userId + "?limit=" + limit;
        try {
            ChatHistoryResponse response = restClient.getForObject(historyRequestUrl, ChatHistoryResponse.class);
            if (response == null) {
                return new ChatHistoryResponse(false, java.util.List.of());
            }
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("FastAPI history endpoint not found at {}. Body: {}", historyRequestUrl,
                    e.getResponseBodyAsString());
            return new ChatHistoryResponse(false, java.util.List.of());
        } catch (HttpClientErrorException e) {
            log.error("FastAPI history request failed with status {} at {}. Body: {}",
                    e.getStatusCode(), historyRequestUrl, e.getResponseBodyAsString());
            return new ChatHistoryResponse(false, java.util.List.of());
        } catch (Exception e) {
            log.error("Error fetching history from FastAPI at {}", historyRequestUrl, e);
            return new ChatHistoryResponse(false, java.util.List.of());
        }
    }

    @SuppressWarnings("null")
    public ChatConversationResponse conversation(@NonNull Long userId, @NonNull Long conversationId) {
        String conversationRequestUrl = historyUrl + "/" + userId + "/" + conversationId;
        try {
            ChatConversationResponse response = restClient.getForObject(conversationRequestUrl,
                    ChatConversationResponse.class);
            if (response == null) {
                return new ChatConversationResponse(false, null);
            }
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("FastAPI conversation endpoint not found at {}. Body: {}", conversationRequestUrl,
                    e.getResponseBodyAsString());
            return new ChatConversationResponse(false, null);
        } catch (HttpClientErrorException e) {
            log.error("FastAPI conversation request failed with status {} at {}. Body: {}",
                    e.getStatusCode(), conversationRequestUrl, e.getResponseBodyAsString());
            return new ChatConversationResponse(false, null);
        } catch (Exception e) {
            log.error("Error fetching conversation from FastAPI at {}", conversationRequestUrl, e);
            return new ChatConversationResponse(false, null);
        }
    }
}