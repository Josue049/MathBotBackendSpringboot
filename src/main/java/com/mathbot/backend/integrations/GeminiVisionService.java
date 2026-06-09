package com.mathbot.backend.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GeminiVisionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String pythonApiUrl;

    public GeminiVisionService(
            // Aquí inyectamos la URL de tu API desplegada en Vercel
            @Value("${app.python-api.url:http://localhost:8000/analyze}") String pythonApiUrl) {
        this.pythonApiUrl = pythonApiUrl;
    }

    public String analyzeExerciseImage(MultipartFile file, List<Map<String, String>> memory) {
        try {
            // 1. Construir el cuerpo Multipart para reenviar el archivo exacto a Python
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                   .contentType(file.getContentType() != null ? MediaType.parseMediaType(file.getContentType()) : MediaType.IMAGE_JPEG);

            MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

            // 2. Configurar cabeceras obligatorias para envío de formularios/archivos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, HttpEntity<?>>> entity = new HttpEntity<>(multipartBody, headers);

            // 3. Realizar la petición POST a tu API de Vercel
            String response = restTemplate.postForObject(pythonApiUrl, entity, String.class);

            // 4. Extraer el campo "text" devuelto por tu script de Python
            JsonNode root = objectMapper.readTree(response);
            if (root.has("text")) {
                return root.path("text").asText();
            }

            throw new IllegalStateException("La API de Python no retornó el campo 'text' esperado.");

        } catch (Exception e) {
            throw new IllegalStateException("Error al conectar con el microservicio de Python: " + e.getMessage(), e);
        }
    }

    public String toDataUrl(MultipartFile file) {
        try {
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String base64 = encoder.encodeToString(file.getBytes());
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            throw new IllegalStateException("Error procesando imagen para DataURL", e);
        }
    }
}