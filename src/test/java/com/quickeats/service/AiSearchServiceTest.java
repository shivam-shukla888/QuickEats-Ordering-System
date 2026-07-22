package com.quickeats.service;

import com.quickeats.dto.SearchFilterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AiSearchServiceTest {

    private AiSearchService aiSearchService;

    @BeforeEach
    void setUp() {
        aiSearchService = new AiSearchService();
        ReflectionTestUtils.setField(aiSearchService, "apiKey", "invalid_test_key");
        ReflectionTestUtils.setField(aiSearchService, "baseUrl", "https://api.groq.com/openai/v1");
        ReflectionTestUtils.setField(aiSearchService, "modelName", "llama-3.1-8b-instant");
    }

    @Test
    void parseSearchQuery_WithEmptyInput_ReturnsEmptyFilter() {
        SearchFilterDTO filter = aiSearchService.parseSearchQuery("");
        assertNotNull(filter);
        assertTrue(filter.getKeywords().isEmpty());
    }

    @Test
    void parseSearchQuery_WhenApiFails_ReturnsFallbackKeywords() {
        SearchFilterDTO filter = aiSearchService.parseSearchQuery("spicy chicken biryani under 15");

        assertNotNull(filter);
        assertNotNull(filter.getKeywords());
        assertFalse(filter.getKeywords().isEmpty());
        assertTrue(filter.getKeywords().contains("spicy"));
        assertTrue(filter.getKeywords().contains("biryani"));
    }
}
