package com.quickeats.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MenuRetriever {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    public List<DishMatch> retrieveTopMatches(String cravingQuery, int topK) {
        if (cravingQuery == null || cravingQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Embedding queryEmbedding = embeddingModel.embed(cravingQuery).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, topK, 0.1);

        List<DishMatch> result = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            TextSegment segment = match.embedded();
            Double score = match.score();

            String menuIdStr = segment.metadata().get("menuId");
            Long menuId = menuIdStr != null ? Long.parseLong(menuIdStr) : 1L;
            String itemName = segment.metadata().get("itemName");
            String priceStr = segment.metadata().get("price");
            Double price = priceStr != null ? Double.parseDouble(priceStr) : 0.0;
            String restaurantName = segment.metadata().get("restaurantName");

            result.add(new DishMatch(menuId, itemName, price, restaurantName, segment.text(), score));
        }

        return result;
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
