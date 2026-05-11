package com.smartbpm.demo.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbpm.demo.config.SmartBpmProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class OpenAiGateway implements AiGateway {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final SmartBpmProperties properties;

    public OpenAiGateway(WebClient webClient, ObjectMapper objectMapper, SmartBpmProperties properties) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String generateStructuredJson(String prompt) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is missing. Use provider=fake for local demo without credentials.");
        }

        Map<String, Object> payload = Map.of(
                "model", properties.getAi().getModel(),
                "messages", new Object[] {
                        Map.of("role", "system", "content", "You are a BPMN analyst. Reply ONLY with valid JSON matching the requested schema."),
                        Map.of("role", "user", "content", prompt)
                },
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.1
        );

        String response = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse OpenAI response", e);
        }
    }
}
