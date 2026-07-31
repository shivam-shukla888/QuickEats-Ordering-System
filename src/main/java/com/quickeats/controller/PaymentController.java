package com.quickeats.controller;

import com.quickeats.dto.PaymentResponseDTO;
import com.quickeats.dto.PaymentVerifyDTO;
import com.quickeats.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> createPaymentOrder(@PathVariable Long orderId) {
        PaymentResponseDTO response = paymentService.createPaymentOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDTO> verifyPayment(@Valid @RequestBody PaymentVerifyDTO verifyDTO) {
        PaymentResponseDTO response = paymentService.verifyAndProcessPayment(verifyDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fail/{orderId}")
    public ResponseEntity<PaymentResponseDTO> handlePaymentFailure(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Payment failed or cancelled by user") : "Payment failed";
        PaymentResponseDTO response = paymentService.handlePaymentFailure(orderId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> razorpayWebhook(@RequestBody Map<String, Object> payload) {
        // Razorpay webhook event processing
        try {
            String event = (String) payload.get("event");
            if ("payment.captured".equals(event) || "order.paid".equals(event)) {
                Map<String, Object> payloadData = (Map<String, Object>) payload.get("payload");
                if (payloadData != null) {
                    Map<String, Object> paymentObj = (Map<String, Object>) payloadData.get("payment");
                    if (paymentObj != null) {
                        Map<String, Object> entity = (Map<String, Object>) paymentObj.get("entity");
                        if (entity != null) {
                            String rzpOrderId = (String) entity.get("order_id");
                            String rzpPaymentId = (String) entity.get("id");
                            PaymentVerifyDTO verifyDTO = new PaymentVerifyDTO();
                            verifyDTO.setRazorpayOrderId(rzpOrderId);
                            verifyDTO.setRazorpayPaymentId(rzpPaymentId);
                            paymentService.verifyAndProcessPayment(verifyDTO);
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("status", "webhook_processed"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "processed_with_notice", "message", e.getMessage()));
        }
    }
}
