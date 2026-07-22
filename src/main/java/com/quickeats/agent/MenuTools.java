package com.quickeats.agent;

import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.RestaurantRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuTools {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Tool("Search restaurant menus for matching dishes based on keywords, maximum price, or restaurant name.")
    public String searchMenu(String query, Double maxPrice, String restaurant) {
        ToolInvocationTracker.logToolCall("searchMenu");

        List<Menu> allMenus = menuRepository.findAll();

        List<Menu> filtered = allMenus.stream().filter(menu -> {
            boolean matchesQuery = true;
            if (query != null && !query.trim().isEmpty()) {
                String q = query.toLowerCase();
                matchesQuery = (menu.getItemName() != null && menu.getItemName().toLowerCase().contains(q)) ||
                               (menu.getDescription() != null && menu.getDescription().toLowerCase().contains(q)) ||
                               (menu.getTags() != null && menu.getTags().toLowerCase().contains(q)) ||
                               (menu.getSpiceLevel() != null && menu.getSpiceLevel().name().toLowerCase().contains(q));
            }

            boolean matchesPrice = true;
            if (maxPrice != null && maxPrice > 0) {
                matchesPrice = menu.getPrice() <= maxPrice;
            }

            boolean matchesRestaurant = true;
            if (restaurant != null && !restaurant.trim().isEmpty()) {
                String restName = menu.getRestaurant() != null ? menu.getRestaurant().getName().toLowerCase() : "";
                matchesRestaurant = restName.contains(restaurant.toLowerCase());
            }

            return matchesQuery && matchesPrice && matchesRestaurant;
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return "No matching menu items found for query '" + query + "' under max price ₹" + (maxPrice != null ? maxPrice : "unlimited") + ".";
        }

        double surgeMultiplier = calculateAiSurgeMultiplier();

        StringBuilder sb = new StringBuilder("Found " + filtered.size() + " matching items:\n");
        for (Menu item : filtered) {
            double dynamicPrice = Math.round(item.getPrice() * surgeMultiplier);
            String restName = item.getRestaurant() != null ? item.getRestaurant().getName() : "North Indian Kitchen";
            sb.append(String.format("• [Item ID: %d] %s — ₹%.2f (Original: ₹%.2f) | Restaurant: %s | Veg: %s | Spice: %s\n  Description: %s\n",
                    item.getId(),
                    item.getItemName(),
                    dynamicPrice,
                    item.getPrice(),
                    restName,
                    item.getIsVeg() != null && item.getIsVeg() ? "Yes" : "No",
                    item.getSpiceLevel() != null ? item.getSpiceLevel().name() : "MEDIUM",
                    item.getDescription() != null ? item.getDescription() : "Authentic North Indian Dhaba delicacy"
            ));
        }

        return sb.toString();
    }

    @Autowired
    private com.quickeats.rag.RecommendationService recommendationService;

    @Tool("Recommend semantic dish matches based on natural language craving description like 'spicy comfort food' or 'light healthy dinner'")
    public String recommendDishes(String craving) {
        ToolInvocationTracker.logToolCall("recommendDishes");
        com.quickeats.rag.RecommendationService.RecommendationResult result = recommendationService.getRecommendations(1L, craving);
        return result.getExplanation();
    }

    private double calculateAiSurgeMultiplier() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        if (hour >= 19 && hour <= 23) return 1.10; // +10% dinner peak
        if (hour >= 12 && hour <= 15) return 1.05; // +5% lunch rush
        if (hour >= 23 || hour < 5) return 1.08;   // +8% night surge
        return 0.92;                              // -8% off-peak deal
    }
}
