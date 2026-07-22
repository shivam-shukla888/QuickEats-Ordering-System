package com.quickeats.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceTokenRequestDTO {

    @NotBlank(message = "Device token is required")
    private String deviceToken;

    public DeviceTokenRequestDTO() {
    }

    public DeviceTokenRequestDTO(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
