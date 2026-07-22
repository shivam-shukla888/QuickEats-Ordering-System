package com.quickeats.dto;

import com.quickeats.model.OrderStatus;

public class OrderStatusUpdateDTO {
    private Long orderId;
    private OrderStatus status;
    private String riderName;
    private String riderPhone;
    private Integer etaMinutes;
    private Double lat;
    private Double lng;

    public OrderStatusUpdateDTO() {}

    public OrderStatusUpdateDTO(Long orderId, OrderStatus status, String riderName, String riderPhone, Integer etaMinutes, Double lat, Double lng) {
        this.orderId = orderId;
        this.status = status;
        this.riderName = riderName;
        this.riderPhone = riderPhone;
        this.etaMinutes = etaMinutes;
        this.lat = lat;
        this.lng = lng;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getRiderPhone() {
        return riderPhone;
    }

    public void setRiderPhone(String riderPhone) {
        this.riderPhone = riderPhone;
    }

    public Integer getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(Integer etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
