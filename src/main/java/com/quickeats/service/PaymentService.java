package com.quickeats.service;

import com.quickeats.dto.PaymentResponseDTO;
import com.quickeats.dto.PaymentVerifyDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Order;
import com.quickeats.model.Payment;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Value("${razorpay.key.id:rzp_test_placeholderKeyId}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:placeholderKeySecret}")
    private String razorpayKeySecret;

    @Transactional
    public PaymentResponseDTO createPaymentOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Optional<Payment> existingOpt = paymentRepository.findByOrderId(orderId);
        if (existingOpt.isPresent()) {
            Payment existing = existingOpt.get();
            if ("SUCCESS".equalsIgnoreCase(existing.getStatus())) {
                return PaymentResponseDTO.fromEntity(existing, razorpayKeyId);
            }
        }

        // Generate test-mode Razorpay order ID
        String razorpayOrderId = "order_rzp_" + orderId + "_" + System.currentTimeMillis();
        Double amountInINR = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;

        Payment payment = existingOpt.orElseGet(() -> new Payment(order, razorpayOrderId, amountInINR, "INR"));
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setAmount(amountInINR);
        payment.setStatus("PENDING");

        Payment saved = paymentRepository.save(payment);
        logger.info("Created Razorpay payment order {} for Order #{}", razorpayOrderId, orderId);

        return PaymentResponseDTO.fromEntity(saved, razorpayKeyId);
    }

    @Transactional
    public PaymentResponseDTO verifyAndProcessPayment(PaymentVerifyDTO verifyDTO) {
        Payment payment = null;
        if (verifyDTO.getRazorpayOrderId() != null) {
            payment = paymentRepository.findByRazorpayOrderId(verifyDTO.getRazorpayOrderId()).orElse(null);
        }
        if (payment == null && verifyDTO.getOrderId() != null) {
            payment = paymentRepository.findByOrderId(verifyDTO.getOrderId()).orElse(null);
        }
        if (payment == null) {
            throw new ResourceNotFoundException("Payment record not found for the given order details");
        }

        payment.setRazorpayPaymentId(verifyDTO.getRazorpayPaymentId() != null ? verifyDTO.getRazorpayPaymentId() : "pay_" + UUID.randomUUID().toString().substring(0, 10));
        payment.setRazorpaySignature(verifyDTO.getRazorpaySignature() != null ? verifyDTO.getRazorpaySignature() : "sig_" + UUID.randomUUID().toString().substring(0, 10));
        payment.setStatus("SUCCESS");

        Payment savedPayment = paymentRepository.save(payment);
        Order order = savedPayment.getOrder();

        // Move order from PENDING to CONFIRMED and broadcast WebSocket status
        orderService.updateOrderStatus(order.getId(), "CONFIRMED");
        logger.info("Payment SUCCESS for Order #{}. Order status updated to CONFIRMED.", order.getId());

        return PaymentResponseDTO.fromEntity(savedPayment, razorpayKeyId);
    }

    @Transactional
    public PaymentResponseDTO handlePaymentFailure(Long orderId, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order id: " + orderId));

        payment.setStatus("FAILED");
        Payment saved = paymentRepository.save(payment);

        // Move order status to PAYMENT_FAILED
        orderService.updateOrderStatus(orderId, "PAYMENT_FAILED");
        logger.warn("Payment FAILED for Order #{}: {}. Order status updated to PAYMENT_FAILED.", orderId, reason);

        return PaymentResponseDTO.fromEntity(saved, razorpayKeyId);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return PaymentResponseDTO.fromEntity(payment, razorpayKeyId);
    }
}
