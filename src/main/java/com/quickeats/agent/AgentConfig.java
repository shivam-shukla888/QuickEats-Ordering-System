package com.quickeats.agent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class AgentConfig {

    private static final Logger logger = LoggerFactory.getLogger(AgentConfig.class);

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.model-name:llama-3.1-8b-instant}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        String effectiveKey = (apiKey != null && !apiKey.trim().isEmpty())
                ? apiKey
                : System.getenv().getOrDefault("GROQ_API_KEY", System.getenv().getOrDefault("OPENAI_API_KEY", ""));

        if (effectiveKey == null || effectiveKey.trim().isEmpty()) {
            logger.warn("No GROQ_API_KEY or OPENAI_API_KEY found. Initializing safe fallback ChatLanguageModel stub.");
            return createStubChatModel("AI API Key is missing. Deterministic tool fallback active.");
        }

        try {
            logger.info("Initializing OpenAiChatModel with model '{}' and base URL '{}'.", modelName, baseUrl);
            return OpenAiChatModel.builder()
                    .apiKey(effectiveKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(0.2)
                    .timeout(Duration.ofSeconds(15))
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to initialize OpenAiChatModel: {}. Using fallback ChatLanguageModel stub.", e.getMessage());
            return createStubChatModel("AI model initialization error: " + e.getMessage());
        }
    }

    private ChatLanguageModel createStubChatModel(String fallbackMessage) {
        return new ChatLanguageModel() {
            @Override
            public String generate(String userMessage) {
                return fallbackMessage;
            }

            @Override
            public Response<AiMessage> generate(List<ChatMessage> messages) {
                return Response.from(AiMessage.from(fallbackMessage));
            }
        };
    }
}
