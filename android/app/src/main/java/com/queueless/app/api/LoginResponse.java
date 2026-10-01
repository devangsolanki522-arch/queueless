package com.queueless.app.api;

public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}