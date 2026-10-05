package org.c2.supportticketsystem.dto.request;

import jakarta.validation.constraints.NotNull;

import org.c2.supportticketsystem.model.enums.Status;

/**
 * Request DTO for transitioning a ticket to a new status.
 */
public class TransitionRequest {

    @NotNull
    private Status targetStatus;

    public TransitionRequest() {
    }

    public TransitionRequest(Status targetStatus) {
        this.targetStatus = targetStatus;
    }

    public Status getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(Status targetStatus) {
        this.targetStatus = targetStatus;
    }
}
