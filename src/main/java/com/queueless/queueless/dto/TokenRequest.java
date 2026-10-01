package com.queueless.queueless.dto;

import jakarta.validation.constraints.NotNull;

public class TokenRequest {

    @NotNull
    private Long serviceId;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}