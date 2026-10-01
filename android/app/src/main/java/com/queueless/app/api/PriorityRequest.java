package com.queueless.app.api;

public class PriorityRequest {

    private String priority;

    public PriorityRequest(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
