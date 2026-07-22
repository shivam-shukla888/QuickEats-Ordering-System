package com.quickeats.controller;

import com.quickeats.dto.ChatRequestDTO;
import com.quickeats.dto.ChatResponseDTO;
import com.quickeats.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chatWithAi(@Valid @RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = aiAssistantService.chatWithAssistant(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
