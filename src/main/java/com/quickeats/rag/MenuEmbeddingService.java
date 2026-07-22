package com.quickeats.rag;

import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(MenuEmbeddingService.class);

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @PostConstruct
    public void init() {
        reindexMenuEmbeddings();
    }

    public synchronized int reindexMenuEmbeddings() {
        List<Menu> menus = menuRepository.findAll();
        if (menus.isEmpty()) {
            logger.info("No menu items found in database to embed.");
            return 0;
        }

        int count = 0;
        for (Menu menu : menus) {
            String restName = menu.getRestaurant() != null ? menu.getRestaurant().getName() : "North Indian Kitchen";
            String cuisine = menu.getRestaurant() != null ? menu.getRestaurant().getCuisineType() : "North Indian";

            String text = String.format("Dish: %s. Description: %s. Cuisine: %s. Restaurant: %s. Price: ₹%.2f. Veg: %s. Tags: %s",
                    menu.getItemName(),
                    menu.getDescription() != null ? menu.getDescription() : "Delicious food item",
                    cuisine,
                    restName,
                    menu.getPrice(),
                    menu.getIsVeg() != null && menu.getIsVeg() ? "Veg" : "Non-Veg",
                    menu.getTags() != null ? menu.getTags() : "North Indian"
            );

            Metadata metadata = new Metadata();
            metadata.put("menuId", String.valueOf(menu.getId()));
            metadata.put("itemName", menu.getItemName());
            metadata.put("price", String.valueOf(menu.getPrice()));
            metadata.put("restaurantName", restName);

            TextSegment textSegment = TextSegment.from(text, metadata);
            Embedding embedding = embeddingModel.embed(textSegment).content();
            embeddingStore.add(embedding, textSegment);
            count++;
        }

        logger.info("Successfully generated vector embeddings for {} menu items using AllMiniLmL6V2.", count);
        return count;
    }
}
