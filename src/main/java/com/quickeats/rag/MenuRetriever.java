package com.quickeats.rag;

import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuRetriever {

    @Autowired
    private MenuRepository menuRepository;

    public List<DishMatch> retrieveTopMatches(String cravingQuery, int topK) {
        if (cravingQuery == null || cravingQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String queryLower = cravingQuery.toLowerCase();
        List<Menu> allMenus = menuRepository.findAll();

        List<DishMatch> matches = allMenus.stream()
                .filter(menu -> {
                    String name = menu.getItemName() != null ? menu.getItemName().toLowerCase() : "";
                    String desc = menu.getDescription() != null ? menu.getDescription().toLowerCase() : "";
                    String tags = menu.getTags() != null ? menu.getTags().toLowerCase() : "";
                    return name.contains(queryLower) || desc.contains(queryLower) || tags.contains(queryLower);
                })
                .limit(topK > 0 ? topK : 4)
                .map(menu -> {
                    String restName = menu.getRestaurant() != null ? menu.getRestaurant().getName() : "QuickEats Kitchen";
                    String text = String.format("Dish: %s. Description: %s. Restaurant: %s. Price: ₹%.2f",
                            menu.getItemName(),
                            menu.getDescription() != null ? menu.getDescription() : "Delicious food item",
                            restName,
                            menu.getPrice());
                    return new DishMatch(menu.getId(), menu.getItemName(), menu.getPrice(), restName, text, 0.95);
                })
                .collect(Collectors.toList());

        // If no keyword match found, return top dishes as fallback
        if (matches.isEmpty() && !allMenus.isEmpty()) {
            matches = allMenus.stream()
                    .limit(topK > 0 ? topK : 4)
                    .map(menu -> {
                        String restName = menu.getRestaurant() != null ? menu.getRestaurant().getName() : "QuickEats Kitchen";
                        String text = String.format("Dish: %s. Description: %s. Restaurant: %s. Price: ₹%.2f",
                                menu.getItemName(),
                                menu.getDescription() != null ? menu.getDescription() : "Delicious food item",
                                restName,
                                menu.getPrice());
                        return new DishMatch(menu.getId(), menu.getItemName(), menu.getPrice(), restName, text, 0.85);
                    })
                    .collect(Collectors.toList());
        }

        return matches;
    }

    public static class DishMatch {
        private Long menuId;
        private String itemName;
        private Double price;
        private String restaurantName;
        private String fullText;
        private Double similarityScore;

        public DishMatch(Long menuId, String itemName, Double price, String restaurantName, String fullText, Double similarityScore) {
            this.menuId = menuId;
            this.itemName = itemName;
            this.price = price;
            this.restaurantName = restaurantName;
            this.fullText = fullText;
            this.similarityScore = similarityScore;
        }

        public Long getMenuId() { return menuId; }
        public String getItemName() { return itemName; }
        public Double getPrice() { return price; }
        public String getRestaurantName() { return restaurantName; }
        public String getFullText() { return fullText; }
        public Double getSimilarityScore() { return similarityScore; }
    }
}
