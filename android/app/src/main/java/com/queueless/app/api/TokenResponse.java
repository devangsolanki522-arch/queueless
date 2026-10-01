package com.queueless.app.api;

public class TokenResponse {

    private Long id;
    private int tokenNumber;
    private String priority;
    private String status;
    private Long serviceId;
    private Long customerId;

    public Long getId() {
        return id;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
