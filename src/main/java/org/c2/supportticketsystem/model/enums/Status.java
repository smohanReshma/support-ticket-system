package org.c2.supportticketsystem.model.enums;

/**
 * Represents the status of a ticket.
 * 
 * <p>Valid status values according to the state machine:
 * <ul>
 *   <li>OPEN - New ticket, not yet worked on</li>
 *   <li>IN_PROGRESS - Ticket is being worked on</li>
 *   <li>RESOLVED - Issue has been resolved</li>
 *   <li>CANCELLED - Ticket was cancelled</li>
 *   <li>CLOSED - Resolved ticket has been closed</li>
 * </ul>
 */
public enum Status {
    /**
     * New ticket, not yet worked on.
     */
    OPEN,

    /**
     * Ticket is being worked on.
     */
    IN_PROGRESS,

    /**
     * Issue has been resolved.
     */
    RESOLVED,

    /**
     * Ticket was cancelled.
     */
    CANCELLED,

    /**
     * Resolved ticket has been closed.
     */
    CLOSED
}
