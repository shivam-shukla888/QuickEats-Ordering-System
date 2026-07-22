package com.quickeats.rag;

import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import com.quickeats.repository.MenuRepository;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuRagTest {

    @Mock
    private MenuRepository menuRepository;

    @Spy
    private EmbeddingModel embeddingModel = new dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel();

    @Spy
    private EmbeddingStore<TextSegment> embeddingStore = new dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore<>();

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @InjectMocks
    private MenuEmbeddingService menuEmbeddingService;

    @InjectMocks
    private MenuRetriever menuRetriever;

    @InjectMocks
    private RecommendationService recommendationService;

    private Restaurant sampleRestaurant;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recommendationService, "menuRetriever", menuRetriever);
        ReflectionTestUtils.setField(recommendationService, "chatLanguageModel", chatLanguageModel);

        sampleRestaurant = new Restaurant("Punjab Dhaba", "Connaught Place", "North Indian");
        menu1 = new Menu(sampleRestaurant, "Paneer Butter Masala", 220.0, "Rich cottage cheese in creamy tomato gravy", true, com.quickeats.model.SpiceLevel.MEDIUM, "North Indian,Veg");
        menu1.setId(1L);

        menu2 = new Menu(sampleRestaurant, "Chicken Biryani", 350.0, "Spicy aromatic basmati rice cooked with chicken", false, com.quickeats.model.SpiceLevel.HOT, "Biryani,Spicy");
        menu2.setId(2L);
    }

    @Test
    void testEmbeddingsCreatedForSampleMenuItems() {
        when(menuRepository.findAll()).thenReturn(List.of(menu1, menu2));

        int count = menuEmbeddingService.reindexMenuEmbeddings();

        assertEquals(2, count);
    }

    @Test
    void testSimilaritySearchReturnsRelevantResults() {
        when(menuRepository.findAll()).thenReturn(List.of(menu1, menu2));
        menuEmbeddingService.reindexMenuEmbeddings();

        List<MenuRetriever.DishMatch> matches = menuRetriever.retrieveTopMatches("spicy biryani", 2);

        assertNotNull(matches);
        assertFalse(matches.isEmpty());
        assertTrue(matches.get(0).getSimilarityScore() > 0.0);
    }

    @Test
    void testRecommendationServiceCombinesRetrievalAndGeneration() {
        when(menuRepository.findAll()).thenReturn(List.of(menu1, menu2));
        menuEmbeddingService.reindexMenuEmbeddings();
        when(chatLanguageModel.generate(anyString())).thenReturn("We recommend Chicken Biryani (₹350) for your spicy craving!");

        RecommendationService.RecommendationResult result = recommendationService.getRecommendations(1L, "spicy craving");

        assertNotNull(result);
        assertFalse(result.getRecommendedDishes().isEmpty());
        assertFalse(result.getRetrievalScores().isEmpty());
        assertTrue(result.getExplanation().contains("Chicken Biryani"));
    }
}
