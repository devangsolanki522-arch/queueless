package com.queueless.queueless.dto;

import com.queueless.queueless.entity.Priority;
import com.queueless.queueless.entity.TokenStatus;

public class QueueTokenResponse {

    private int tokenNumber;
    private Priority priority;
    private TokenStatus status;
    private int position;
    private int estimatedWaitTime;

    public QueueTokenResponse(
            int tokenNumber,
            Priority priority,
            TokenStatus status,
            int position,
            int estimatedWaitTime
    ) {
        this.tokenNumber = tokenNumber;
        this.priority = priority;
        this.status = status;
        this.position = position;
        this.estimatedWaitTime = estimatedWaitTime;
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

    public int getPosition() {
        return position;
    }

    public int getEstimatedWaitTime() {
        return estimatedWaitTime;
    }
}
