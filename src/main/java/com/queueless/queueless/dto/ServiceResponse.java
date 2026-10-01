package com.queueless.queueless.dto;

public class ServiceResponse {

    private Long id;
    private String name;
    private int estimatedServiceTime;
    private Long staffId;

    public ServiceResponse(
            Long id,
            String name,
            int estimatedServiceTime,
            Long staffId
    ) {
        this.id = id;
        this.name = name;
        this.estimatedServiceTime = estimatedServiceTime;
        this.staffId = staffId;
    }

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