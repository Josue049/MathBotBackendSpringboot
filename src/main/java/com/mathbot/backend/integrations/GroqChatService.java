package com.mathbot.backend.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GroqChatService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final double temperature;

    public GroqChatService(
            @Value("${app.groq.api-key:}") String apiKey,
            @Value("${app.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${app.groq.model:openai/gpt-oss-20b}") String model,
            @Value("${app.groq.temperature:0.4}") double temperature) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.temperature = temperature;
        System.out.println("####################################API KEY: " + apiKey);
    }

    public String tutorSystemPrompt() {
        return "Eres MathBot, tutor de matemáticas para niñas y niños de primaria (6-12 años). "
                + "Responde SIEMPRE en español con tono amable, motivador y claro. "
                + "Reglas estrictas: "
                + "1) Explica en pasos cortos y numerados. "
                + "2) No resuelvas todo de una vez; guía una parte y espera al estudiante. "
                + "3) Cada respuesta debe incluir al menos un ejemplo en bloque de código con formato ```txt```. "
                + "4) Mantén respuestas breves (máximo 120 palabras). "
                + "5) Si hay error del estudiante, corrige con cariño y explica por qué. "
                + "6) Cierra SIEMPRE con una pregunta para continuar.";
    }

    public String generateMathTitle(String firstMessage) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "Genera un título corto de tema matemático en español para una conversación escolar. "
                                + "Debe tener entre 3 y 6 palabras, no llevar comillas ni puntuación final."),
                Map.of("role", "user", "content", "Mensaje inicial del estudiante: " + firstMessage));

        String title = complete(messages, 0.3);
        if (title == null || title.isBlank()) {
            return "Tema de matemáticas";
        }
        return title.trim().substring(0, Math.min(title.trim().length(), 80));
    }

    public String complete(List<Map<String, String>> messages, Double customTemperature) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY no configurada");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", customTemperature != null ? customTemperature : temperature);

        ArrayNode messagesNode = body.putArray("messages");
        for (Map<String, String> message : messages) {
            ObjectNode node = messagesNode.addObject();
            node.put("role", message.get("role"));
            node.put("content", message.get("content"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        String response = restTemplate.postForObject(baseUrl + "/chat/completions", entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer la respuesta de Groq", e);
        }
    }
}
