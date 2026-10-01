package com.queueless.app.api;

public class ServiceResponse {

    private Long id;
    private String name;
    private int estimatedServiceTime;
    private Long staffId;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getEstimatedServiceTime() {
        return estimatedServiceTime;
    }

    public Long getStaffId() {
        return staffId;
    }
}