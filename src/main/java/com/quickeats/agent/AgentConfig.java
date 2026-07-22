package com.quickeats.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AgentConfig {

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
                : System.getenv().getOrDefault("GROQ_API_KEY", "");

        return OpenAiChatModel.builder()
                .apiKey(effectiveKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.2)
                .timeout(Duration.ofSeconds(15))
                .build();
    }
}
