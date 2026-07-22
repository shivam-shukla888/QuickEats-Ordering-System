package com.quickeats.support;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalations")
public class Escalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(length = 1000)
    private String reason;

    private String status;

    private LocalDateTime timestamp;

    public Escalation() {
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING_SUPERVISOR_REVIEW";
    }

    public Escalation(Long orderId, String reason, String status) {
        this.orderId = orderId;
        this.reason = reason;
        this.status = status != null ? status : "PENDING_SUPERVISOR_REVIEW";
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
