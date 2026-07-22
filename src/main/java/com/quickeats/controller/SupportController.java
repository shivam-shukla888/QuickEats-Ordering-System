package com.quickeats.controller;

import com.quickeats.dto.ComplaintRequestDTO;
import com.quickeats.dto.ComplaintResponseDTO;
import com.quickeats.support.SupportAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private static final Logger logger = LoggerFactory.getLogger(SupportController.class);

    @Autowired
    private SupportAgentService supportAgentService;

    @PostMapping("/complaint")
    public ResponseEntity<ComplaintResponseDTO> processComplaint(@RequestBody ComplaintRequestDTO request) {
        logger.info("Received complaint for order #{}: '{}'", request.getOrderId(), request.getComplaint());

        SupportAgentService.ComplaintResult result = supportAgentService.handleComplaint(
                request.getUserId(),
                request.getOrderId(),
                request.getComplaint()
        );

        logger.info("Autonomous Support Agent Decision: {}, Tools Invoked: {}", result.getDecision(), result.getToolsInvoked());

        ComplaintResponseDTO response = new ComplaintResponseDTO(
                result.getInvestigation(),
                result.getClassification(),
                result.getDecision(),
                result.getActionTaken(),
                result.getReasoning(),
                result.getToolsInvoked()
        );

        return ResponseEntity.ok(response);
    }
}
