package com.quickeats.controller;

import com.quickeats.dto.RecommendationRequestDTO;
import com.quickeats.dto.RecommendationResponseDTO;
import com.quickeats.rag.MenuEmbeddingService;
import com.quickeats.rag.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private MenuEmbeddingService menuEmbeddingService;

    @PostMapping
    public ResponseEntity<RecommendationResponseDTO> recommendDishes(@RequestBody RecommendationRequestDTO request) {
        logger.info("Received RAG recommendation request for craving: '{}'", request.getCraving());

        RecommendationService.RecommendationResult result = recommendationService.getRecommendations(
                request.getUserId(),
                request.getCraving()
        );

        logger.info("RAG recommendation completed. Vector retrieval scores: {}", result.getRetrievalScores());

        RecommendationResponseDTO responseDTO = new RecommendationResponseDTO(
                result.getRecommendedDishes(),
                result.getExplanation(),
                result.getRetrievalScores()
        );

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindexVectorEmbeddings() {
        logger.info("Admin triggered vector store reindexing...");
        int count = menuEmbeddingService.reindexMenuEmbeddings();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Successfully refreshed " + count + " menu entities",
                "indexedItemsCount", count
        ));
    }
}
