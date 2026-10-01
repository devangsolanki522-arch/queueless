package com.queueless.queueless.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ServiceRequest {

    @NotBlank
    private String name;

    @Min(1)
    private int estimatedServiceTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEstimatedServiceTime() {
        return estimatedServiceTime;
    }

    public void setEstimatedServiceTime(int estimatedServiceTime) {
        this.estimatedServiceTime = estimatedServiceTime;
    }
}