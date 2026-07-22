package com.quickeats.dto;

public class AuthResponseDTO {

    private String token;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponseDTO user;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, UserResponseDTO user) {
        this.token = token;
        this.accessToken = token;
        this.user = user;
    }

    public AuthResponseDTO(String accessToken, String refreshToken, UserResponseDTO user) {
        this.token = accessToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() {
        return token != null ? token : accessToken;
    }

    public void setToken(String token) {
        this.token = token;
        this.accessToken = token;
    }

    public String getAccessToken() {
        return accessToken != null ? accessToken : token;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.token = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponseDTO getUser() {
        return user;
    }

    public void setUser(UserResponseDTO user) {
        this.user = user;
    }
}
