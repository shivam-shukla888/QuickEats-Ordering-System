package com.quickeats.controller;

import com.quickeats.agent.OrderingAgentService;
import com.quickeats.dto.AgentChatRequestDTO;
import com.quickeats.dto.AgentChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    @Autowired
    private OrderingAgentService orderingAgentService;

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponseDTO> chatWithAgent(@RequestBody AgentChatRequestDTO request) {
        logger.info("Agent received chat request from userId [{}]: {}", request.getUserId(), request.getMessage());

        OrderingAgentService.AgentResponse agentResponse = orderingAgentService.processMessage(
                request.getUserId(),
                request.getMessage()
        );

        logger.info("Agent response generated. Tools invoked: {}", agentResponse.getToolsInvoked());

        AgentChatResponseDTO responseDTO = new AgentChatResponseDTO(
                agentResponse.getResponse(),
                agentResponse.getToolsInvoked()
        );

        return ResponseEntity.ok(responseDTO);
    }
}
