package com.queueless.queueless.dto;

import com.queueless.queueless.entity.Priority;
import jakarta.validation.constraints.NotNull;

public class PriorityRequest {

    @NotNull
    private Priority priority;

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
