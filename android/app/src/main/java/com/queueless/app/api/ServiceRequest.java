package com.queueless.app.api;

public class ServiceRequest {

    private String name;
    private int estimatedServiceTime;

    public ServiceRequest(
            String name,
            int estimatedServiceTime
    ) {
        this.name = name;
        this.estimatedServiceTime =
                estimatedServiceTime;
    }

    public String getName() {
        return name;
    }

    public int getEstimatedServiceTime() {
        return estimatedServiceTime;
    }
}
