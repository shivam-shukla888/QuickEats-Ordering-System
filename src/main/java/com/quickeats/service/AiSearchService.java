package com.quickeats.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.dto.SearchFilterDTO;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class AiSearchService {

    private static final Logger logger = LoggerFactory.getLogger(AiSearchService.class);

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.model-name:llama-3.1-8b-instant}")
    private String modelName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchFilterDTO parseSearchQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return new SearchFilterDTO(null, null, null, null, null, null, null, Collections.emptyList());
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("GROQ_API_KEY environment variable is not set. Falling back to keyword search for query: '{}'", userQuery);
            return createFallbackFilter(userQuery);
        }

        try {
            ChatLanguageModel chatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey.trim())
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(5))
                    .temperature(0.0)
                    .build();

            String systemPrompt = """
                You are an advanced AI food search parser for QuickEats food delivery.
                Analyze the user's natural language input and extract structured filters as ONLY a raw JSON object. Do not output markdown or extra text.
                JSON structure:
                {
                  "restaurantName": string or null (e.g. "Pizza Hut", "Burger King", "Spice Villa", "Dragon Wok", "Sushi Spot"),
                  "cuisineType": string or null (e.g. "Indian", "Italian", "Chinese", "American", "Japanese"),
                  "minPrice": number or null (e.g. 10.0 for "above 10", "between 10 and 20"),
                  "maxPrice": number or null (e.g. 20.0 for "under 20", "below $15"),
                  "isVeg": boolean or null (true for "veg", "vegetarian", "pure veg"; false for "chicken", "beef", "meat", "non-veg"),
                  "spiceLevel": "MILD" or "MEDIUM" or "HOT" or null (e.g. "HOT" for "spicy", "fire", "chili"; "MILD" for "sweet", "plain"),
                  "addressKeywords": string or null (e.g. "Oak Ave", "Curry Lane", "Main St"),
                  "keywords": array of search term strings (e.g. ["pizza", "burger", "biryani", "fries"])
                }
                """;

            String prompt = systemPrompt + "\nUser search query: \"" + userQuery.trim() + "\"";
            String jsonResponse = chatModel.generate(prompt);

            logger.debug("Groq AI raw response: {}", jsonResponse);

            // Clean up possible markdown wrappers ```json ... ```
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            SearchFilterDTO filter = objectMapper.readValue(cleanJson, SearchFilterDTO.class);
            return filter != null ? filter : createFallbackFilter(userQuery);

        } catch (Exception e) {
            logger.warn("AI search parsing failed or timed out for query: '{}'. Falling back to keyword search. Error: {}", userQuery, e.getMessage());
            return createFallbackFilter(userQuery);
        }
    }

    private SearchFilterDTO createFallbackFilter(String userQuery) {
        List<String> keywords = Arrays.stream(userQuery.trim().split("\\s+"))
                .filter(w -> !w.equalsIgnoreCase("under") && !w.equalsIgnoreCase("below") && !w.equalsIgnoreCase("for") && !w.equalsIgnoreCase("show") && !w.equalsIgnoreCase("me"))
                .toList();
        return new SearchFilterDTO(null, null, null, null, null, null, null, keywords);
    }
}
