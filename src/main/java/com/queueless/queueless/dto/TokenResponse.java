package com.queueless.queueless.dto;

import com.queueless.queueless.entity.Priority;
import com.queueless.queueless.entity.TokenStatus;

public class TokenResponse {

    private Long id;
    private int tokenNumber;
    private Priority priority;
    private TokenStatus status;
    private Long serviceId;
    private Long customerId;

    public TokenResponse(
            Long id,
            int tokenNumber,
            Priority priority,
            TokenStatus status,
            Long serviceId,
            Long customerId
    ) {
        this.id = id;
        this.tokenNumber = tokenNumber;
        this.priority = priority;
        this.status = status;
        this.serviceId = serviceId;
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public Priority getPriority() {
        return priority;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}