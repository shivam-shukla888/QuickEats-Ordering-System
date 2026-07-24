package com.quickeats.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.dto.AiRecommendationDTO;
import com.quickeats.model.Menu;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(AiRecommendationService.class);
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[\\s*\\{.*\\}\\s*\\]", Pattern.DOTALL);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private GroqClientService groqClientService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<AiRecommendationDTO> getUserRecommendations(Long userId) {
        logger.info("Generating AI personalized recommendations for userId={}", userId);

        List<Order> userOrders = (userId != null) ? orderRepository.findByUserId(userId) : Collections.emptyList();

        // If user has no order history, fall back to top/popular DB menu items
        if (userOrders.isEmpty()) {
            logger.info("User ID {} has no past order history. Using popular database recommendations fallback.", userId);
            return getFallbackRecommendations();
        }

        // Fetch all available restaurants and menus from DB
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        List<Menu> allMenuItems = menuRepository.findAll();

        if (allMenuItems.isEmpty()) {
            logger.warn("No menu items available in database for recommendation.");
            return Collections.emptyList();
        }

        // Extract user ordering patterns
        Map<String, Integer> cuisineCounts = new HashMap<>();
        double totalSpent = 0.0;
        int totalItemsCount = 0;
        List<String> pastItemDescriptions = new ArrayList<>();

        for (Order order : userOrders) {
            if (order.getRestaurant() != null && order.getRestaurant().getCuisineType() != null) {
                String cuisine = order.getRestaurant().getCuisineType();
                cuisineCounts.put(cuisine, cuisineCounts.getOrDefault(cuisine, 0) + 1);
            }
            if (order.getTotalAmount() != null) {
                totalSpent += order.getTotalAmount();
                totalItemsCount++;
            }
            if (order.getOrderItems() != null) {
                pastItemDescriptions.add(order.getOrderItems());
            }
        }

        String topCuisine = cuisineCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");

        double avgOrderPrice = (totalItemsCount > 0) ? (totalSpent / totalItemsCount) : 15.0;

        StringBuilder availableCatalogStr = new StringBuilder();
        for (Menu menu : allMenuItems) {
            String restName = (menu.getRestaurant() != null) ? menu.getRestaurant().getName() : "QuickEats";
            String cuisine = (menu.getRestaurant() != null) ? menu.getRestaurant().getCuisineType() : "Standard";
            availableCatalogStr.append(String.format("- Item: '%s' | Restaurant: '%s' | Cuisine: '%s' | Price: $%.2f | Description: %s\n",
                    menu.getItemName(),
                    restName,
                    cuisine,
                    menu.getPrice(),
                    menu.getDescription() != null ? menu.getDescription() : "Delicious dish"
            ));
        }

        String systemPrompt = """
                You are QuickEats AI Recommendation Engine.
                Your task is to analyze user food ordering patterns and recommend EXACTLY 3 menu items from the available catalog provided.
                
                OUTPUT FORMAT REQUIREMENTS:
                You MUST return ONLY a raw JSON array containing exactly 3 objects.
                Each object MUST have the following keys:
                - "restaurantName": String (exact restaurant name from the catalog)
                - "itemName": String (exact item name from the catalog)
                - "reason": String (1-2 sentence personalized explanation for why the user will love this item)
                
                DO NOT wrap the response in markdown code blocks like ```json.
                DO NOT include any text, intro, or commentary outside the JSON array.
                """;

        String userPrompt = String.format("""
                User Preferred Cuisine: %s
                Average Order Value: $%.2f
                Past Ordered Items Summary: %s
                
                Available Catalog:
                %s
                
                Recommend 3 items from the catalog above tailored to this user's preferences in the required JSON array format.
                """, topCuisine, avgOrderPrice, String.join("; ", pastItemDescriptions), availableCatalogStr.toString());

        try {
            String rawLlmResponse = groqClientService.chatCompletion(systemPrompt, userPrompt);
            if (rawLlmResponse != null && !rawLlmResponse.trim().isEmpty()) {
                List<AiRecommendationDTO> parsed = parseLlmRecommendations(rawLlmResponse);
                if (parsed != null && !parsed.isEmpty()) {
                    logger.info("Successfully generated {} AI personalized recommendations for userId={}", parsed.size(), userId);
                    return parsed;
                }
            }
        } catch (Exception e) {
            logger.warn("Groq API call or JSON parsing failed for recommendations (userId={}): {}. Falling back to popular items.", userId, e.getMessage());
        }

        return getFallbackRecommendations();
    }

    private List<AiRecommendationDTO> parseLlmRecommendations(String responseText) {
        try {
            String cleanJson = responseText.trim();

            // Strip markdown code block formatting if present
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            Matcher matcher = JSON_ARRAY_PATTERN.matcher(cleanJson);
            if (matcher.find()) {
                cleanJson = matcher.group();
            }

            return objectMapper.readValue(cleanJson, new TypeReference<List<AiRecommendationDTO>>() {});
        } catch (Exception e) {
            logger.warn("Failed to parse LLM recommendation JSON output: {}. Raw output was: '{}'", e.getMessage(), responseText);
            return null;
        }
    }

    /**
     * Fallback recommendation strategy: picks popular/diverse items directly from database.
     */
    public List<AiRecommendationDTO> getFallbackRecommendations() {
        List<Menu> allMenus = menuRepository.findAll();
        if (allMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // Select top 3 items
        List<Menu> selected = allMenus.stream().limit(3).collect(Collectors.toList());

        List<AiRecommendationDTO> result = new ArrayList<>();
        for (Menu item : selected) {
            String restName = (item.getRestaurant() != null) ? item.getRestaurant().getName() : "QuickEats Choice";
            String reason = String.format("A top-rated favorite at %s! Highly popular among QuickEats diners.", restName);
            result.add(new AiRecommendationDTO(restName, item.getItemName(), reason));
        }

        return result;
    }
}
