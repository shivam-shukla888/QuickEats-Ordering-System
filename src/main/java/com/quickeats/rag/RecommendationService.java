package com.quickeats.rag;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private MenuRetriever menuRetriever;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    public RecommendationResult getRecommendations(Long userId, String cravingQuery) {
        if (cravingQuery == null || cravingQuery.trim().isEmpty()) {
            return new RecommendationResult(new ArrayList<>(), "Please describe your food craving!", new ArrayList<>());
        }

        List<MenuRetriever.DishMatch> matches = menuRetriever.retrieveTopMatches(cravingQuery, 4);

        if (matches.isEmpty()) {
            return new RecommendationResult(new ArrayList<>(), "No semantic matches found for your craving: '" + cravingQuery + "'. Try asking for spicy, light, or dhaba dishes!", new ArrayList<>());
        }

        List<Double> retrievalScores = matches.stream().map(MenuRetriever.DishMatch::getSimilarityScore).collect(Collectors.toList());

        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            MenuRetriever.DishMatch m = matches.get(i);
            contextBuilder.append(String.format("%d. [ID: %d] %s (₹%.2f) from %s — Similarity Score: %.4f\n   Text: %s\n",
                    i + 1, m.getMenuId(), m.getItemName(), m.getPrice(), m.getRestaurantName(), m.getSimilarityScore(), m.getFullText()));
        }

        String prompt = String.format("""
            You are QuickEats AI Sommelier and Culinary Recommendation Assistant.
            A customer is asking for: "%s"

            Here are the top semantically retrieved dishes from our vector database:
            %s

            Task:
            Write a warm, appetizing natural language recommendation explaining WHY these retrieved dishes match the customer's craving. Mention item IDs and prices in Indian Rupees (₹).
            """, cravingQuery, contextBuilder.toString());

        String explanation;
        try {
            explanation = chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            logger.warn("LLM generation call failed for RAG recommendation: {}. Using structured fallback explanation.", e.getMessage());
            explanation = "Based on your craving ('" + cravingQuery + "'), we retrieved these top matching dishes from our vector store:\n" +
                    matches.stream().map(m -> String.format("• %s (₹%.2f) [Match Score: %.2f]", m.getItemName(), m.getPrice(), m.getSimilarityScore())).collect(Collectors.joining("\n"));
        }

        List<DishDTO> recommendedDishes = matches.stream().map(m ->
                new DishDTO(m.getMenuId(), m.getItemName(), m.getPrice(), m.getRestaurantName(), m.getSimilarityScore())
        ).collect(Collectors.toList());

        return new RecommendationResult(recommendedDishes, explanation, retrievalScores);
    }

    public static class RecommendationResult {
        private List<DishDTO> recommendedDishes;
        private String explanation;
        private List<Double> retrievalScores;

        public RecommendationResult(List<DishDTO> recommendedDishes, String explanation, List<Double> retrievalScores) {
            this.recommendedDishes = recommendedDishes;
            this.explanation = explanation;
            this.retrievalScores = retrievalScores;
        }

        public List<DishDTO> getRecommendedDishes() { return recommendedDishes; }
        public String getExplanation() { return explanation; }
        public List<Double> getRetrievalScores() { return retrievalScores; }
    }

    public static class DishDTO {
        private Long menuId;
        private String itemName;
        private Double price;
        private String restaurantName;
        private Double similarityScore;

        public DishDTO(Long menuId, String itemName, Double price, String restaurantName, Double similarityScore) {
            this.menuId = menuId;
            this.itemName = itemName;
            this.price = price;
            this.restaurantName = restaurantName;
            this.similarityScore = similarityScore;
        }

        public Long getMenuId() { return menuId; }
        public String getItemName() { return itemName; }
        public Double getPrice() { return price; }
        public String getRestaurantName() { return restaurantName; }
        public Double getSimilarityScore() { return similarityScore; }
    }
}
