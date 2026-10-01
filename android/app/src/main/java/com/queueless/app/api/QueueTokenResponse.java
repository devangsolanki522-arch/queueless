package com.queueless.app.api;

public class QueueTokenResponse {

    private int tokenNumber;
    private String priority;
    private String status;
    private int position;
    private int estimatedWaitTime;

    public int getTokenNumber() {
        return tokenNumber;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public int getPosition() {
        return position;
    }

    public int getEstimatedWaitTime() {
        return estimatedWaitTime;
    }
}
