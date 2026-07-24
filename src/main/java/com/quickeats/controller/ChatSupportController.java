package com.quickeats.controller;

import com.quickeats.dto.SupportChatRequestDTO;
import com.quickeats.dto.SupportChatResponseDTO;
import com.quickeats.service.ChatSupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatSupportController {

    @Autowired
    private ChatSupportService chatSupportService;

    @PostMapping("/support")
    public ResponseEntity<SupportChatResponseDTO> supportChat(@Valid @RequestBody SupportChatRequestDTO request) {
        String reply = chatSupportService.handleSupportChat(request.getUserId(), request.getMessage());
        return ResponseEntity.ok(new SupportChatResponseDTO(reply));
    }
}
