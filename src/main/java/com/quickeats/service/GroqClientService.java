package com.quickeats.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Service
public class GroqClientService {

    private static final Logger logger = LoggerFactory.getLogger(GroqClientService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.model-name:llama-3.3-70b-versatile}")
    private String modelName;

    public GroqClientService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = objectMapper;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Sends a chat completion request with system prompt and user message to Groq API.
     */
    public String chatCompletion(String systemPrompt, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return chatCompletion(messages);
    }

    /**
     * Sends raw list of messages to Groq Chat Completions endpoint.
     */
    public String chatCompletion(List<Map<String, String>> messages) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("GROQ_API_KEY is not configured. Falling back to default handler.");
            throw new IllegalStateException("Groq API key is missing");
        }

        String url = baseUrl + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.info("Executing Groq Chat Completion request to model '{}' with {} messages", modelName, messages.size());
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode messageNode = choices.get(0).path("message");
                    String content = messageNode.path("content").asText();
                    logger.info("Successfully received Groq response of length {}", content.length());
                    return content;
                }
            }
            logger.warn("Groq API returned non-2xx status or empty choices: status={}", response.getStatusCode());
            throw new RuntimeException("Unexpected response status from Groq: " + response.getStatusCode());
        } catch (Exception e) {
            logger.error("Error communicating with Groq API at {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to call Groq API: " + e.getMessage(), e);
        }
    }
}
