package org.c2.supportticketsystem.service;

import org.c2.supportticketsystem.model.enums.Status;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing ticket state transitions.
 * Enforces the state machine rules for ticket status transitions.
 */
@Service
public class StateMachineService {

    /**
     * Check if a status transition is valid according to the state machine.
     *
     * @param currentStatus the current status
     * @param targetStatus  the target status
     * @return true if the transition is valid, false otherwise
     */
    public boolean isValidTransition(Status currentStatus, Status targetStatus) {
        if (currentStatus == targetStatus) {
            return false;
        }

        switch (currentStatus) {
            case OPEN -> {
                return targetStatus == Status.IN_PROGRESS || targetStatus == Status.CANCELLED;
            }
            case IN_PROGRESS -> {
                return targetStatus == Status.RESOLVED || targetStatus == Status.CANCELLED;
            }
            case RESOLVED -> {
                return targetStatus == Status.CLOSED;
            }
            case CANCELLED, CLOSED -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Get the list of valid transitions from the current status.
     *
     * @param currentStatus the current status
     * @return list of valid next status values
     */
    public List<Status> getValidTransitions(Status currentStatus) {
        switch (currentStatus) {
            case OPEN -> {
                return List.of(Status.IN_PROGRESS, Status.CANCELLED);
            }
            case IN_PROGRESS -> {
                return List.of(Status.RESOLVED, Status.CANCELLED);
            }
            case RESOLVED -> {
                return List.of(Status.CLOSED);
            }
            case CANCELLED, CLOSED -> {
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }
}
