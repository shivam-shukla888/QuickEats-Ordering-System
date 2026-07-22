package com.quickeats.dto;

public class ComplaintRequestDTO {
    private Long userId;
    private Long orderId;
    private String complaint;

    public ComplaintRequestDTO() {}

    public ComplaintRequestDTO(Long userId, Long orderId, String complaint) {
        this.userId = userId;
        this.orderId = orderId;
        this.complaint = complaint;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getComplaint() { return complaint; }
    public void setComplaint(String complaint) { this.complaint = complaint; }
}
