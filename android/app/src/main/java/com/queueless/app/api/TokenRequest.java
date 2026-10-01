package com.queueless.app.api;

public class TokenRequest {

    private Long serviceId;

    public TokenRequest(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}