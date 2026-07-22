package com.quickeats.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.dto.ChatResponseDTO;
import com.quickeats.dto.MenuResponseDTO;
import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.RestaurantRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AiAssistantService.class);

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.model-name:llama-3.1-8b-instant}")
    private String modelName;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponseDTO chatWithAssistant(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ChatResponseDTO("Hello! I am your QuickEats AI assistant. How can I help you find delicious meals today?", Collections.emptyList());
        }

        List<Menu> allMenus = menuRepository.findAll();
        String catalogSummary = allMenus.stream()
                .map(m -> String.format("ID:%d, Name:\"%s\", Price:$%.2f, Restaurant:\"%s\", Cuisine:\"%s\", Veg:%b, Spice:%s, Description:\"%s\"",
                        m.getId(), m.getItemName(), m.getPrice(),
                        m.getRestaurant() != null ? m.getRestaurant().getName() : "Unknown",
                        m.getRestaurant() != null ? m.getRestaurant().getCuisineType() : "Unknown",
                        m.getIsVeg(), m.getSpiceLevel(), m.getDescription()))
                .collect(Collectors.joining("\n"));

        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("GROQ_API_KEY missing during AI Chat assistant call.");
            return createFallbackResponse(userMessage, allMenus);
        }

        try {
            ChatLanguageModel chatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey.trim())
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(8))
                    .temperature(0.3)
                    .build();

            String systemPrompt = """
                You are QuickEats AI, a friendly and smart food concierge assistant for the QuickEats delivery platform.
                Analyze the user's message using our menu catalog below and respond with ONLY a raw JSON object.
                JSON shape must be:
                {
                  "reply": "Friendly conversational advice or recommendation text",
                  "recommendedMenuIds": [array of integer menu IDs matching the recommendation]
                }
                
                Available Menu Catalog:
                """ + catalogSummary;

            String prompt = systemPrompt + "\n\nUser Question: \"" + userMessage.trim() + "\"";
            String jsonResponse = chatModel.generate(prompt);

            logger.debug("Groq AI Chat raw response: {}", jsonResponse);

            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
            if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
            if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            cleanJson = cleanJson.trim();

            JsonNode rootNode = objectMapper.readTree(cleanJson);
            String reply = rootNode.path("reply").asText("Here are some food options for you!");
            
            List<Long> recommendedIds = new ArrayList<>();
            JsonNode idsNode = rootNode.path("recommendedMenuIds");
            if (idsNode.isArray()) {
                for (JsonNode idNode : idsNode) {
                    recommendedIds.add(idNode.asLong());
                }
            }

            List<MenuResponseDTO> recommendations = allMenus.stream()
                    .filter(m -> recommendedIds.contains(m.getId()))
                    .map(MenuResponseDTO::fromEntity)
                    .collect(Collectors.toList());

            return new ChatResponseDTO(reply, recommendations);

        } catch (Exception e) {
            logger.warn("AI Chat Assistant failed. Error: {}", e.getMessage());
            return createFallbackResponse(userMessage, allMenus);
        }
    }

    private ChatResponseDTO createFallbackResponse(String userMessage, List<Menu> allMenus) {
        String msgLower = userMessage.toLowerCase();
        List<MenuResponseDTO> matches = allMenus.stream()
                .filter(m -> msgLower.contains("spicy") && Boolean.TRUE.equals(m.getSpiceLevel() != null && m.getSpiceLevel().name().equalsIgnoreCase("HOT"))
                        || msgLower.contains("veg") && Boolean.TRUE.equals(m.getIsVeg())
                        || msgLower.contains(m.getItemName().toLowerCase()))
                .limit(3)
                .map(MenuResponseDTO::fromEntity)
                .collect(Collectors.toList());

        String reply = matches.isEmpty()
                ? "I'm here to help! Try exploring our featured restaurants on the home page or search by cuisine."
                : "Based on your request, here are top recommendations from our catalog!";

        return new ChatResponseDTO(reply, matches);
    }
}
