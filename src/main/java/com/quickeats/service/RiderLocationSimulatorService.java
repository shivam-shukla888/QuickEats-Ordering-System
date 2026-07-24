package com.quickeats.service;

import com.quickeats.model.Order;
import com.quickeats.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiderLocationSimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(RiderLocationSimulatorService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    // Track simulated progress step per order (0.0 to 1.0)
    private final Map<Long, Double> orderProgressMap = new ConcurrentHashMap<>();

    // Mock Route coordinates (Delhi Connaught Place region)
    // Start (Restaurant): 28.6328, 77.2197
    // End (Customer): 28.6245, 77.2140
    private static final double START_LAT = 28.6328;
    private static final double START_LNG = 77.2197;
    private static final double END_LAT = 28.6245;
    private static final double END_LNG = 77.2140;

    @Scheduled(fixedRate = 3000)
    public void simulateRiderMovement() {
        List<Order> activeOrders = orderRepository.findAll().stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .toList();

        for (Order order : activeOrders) {
            double currentProgress = orderProgressMap.getOrDefault(order.getId(), 0.0);

            if ("DELIVERED".equalsIgnoreCase(order.getStatus())) {
                continue;
            }

            // Increment progress by 0.05 per 3-second tick (~60s full cycle)
            currentProgress += 0.05;
            if (currentProgress > 1.0) {
                currentProgress = 1.0;
            }
            orderProgressMap.put(order.getId(), currentProgress);

            // Calculate auto-progressed order status
            String targetStatus = order.getStatus();
            if (currentProgress >= 0.95) {
                targetStatus = "DELIVERED";
            } else if (currentProgress >= 0.50) {
                targetStatus = "OUT_FOR_DELIVERY";
            } else if (currentProgress >= 0.20) {
                targetStatus = "PREPARING";
            }

            if (!targetStatus.equalsIgnoreCase(order.getStatus())) {
                order.setStatus(targetStatus);
                orderRepository.save(order);
                logger.info("Auto-progressed order #{} status to '{}'", order.getId(), targetStatus);
            }

            // Interpolate GPS coordinates
            double currentLat = START_LAT + (END_LAT - START_LAT) * currentProgress;
            double currentLng = START_LNG + (END_LNG - START_LNG) * currentProgress;
            int eta = Math.max(1, (int) Math.round((1.0 - currentProgress) * 20));

            orderService.broadcastStatusUpdate(order, eta, currentLat, currentLng);
            logger.debug("Simulated rider position & status for order #{}: status={}, ({}, {})", order.getId(), targetStatus, currentLat, currentLng);
        }
    }

    public Map<Long, Double> getOrderProgressMap() {
        return orderProgressMap;
    }
}
