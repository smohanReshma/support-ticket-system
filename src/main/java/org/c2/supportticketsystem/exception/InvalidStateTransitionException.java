package org.c2.supportticketsystem.exception;

import org.c2.supportticketsystem.model.enums.Status;

import java.util.List;

/**
 * Exception thrown when an invalid state transition is attempted.
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final Status currentStatus;
    private final Status requestedStatus;
    private final List<Status> validTransitions;

    public InvalidStateTransitionException(Status currentStatus, Status requestedStatus, List<Status> validTransitions) {
        super(String.format("Invalid state transition: %s -> %s", currentStatus, requestedStatus));
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
        this.validTransitions = validTransitions;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public Status getRequestedStatus() {
        return requestedStatus;
    }

    public List<Status> getValidTransitions() {
        return validTransitions;
    }
}
