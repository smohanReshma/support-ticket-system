package org.c2.supportticketsystem.exception;

/**
 * Exception thrown when a ticket is not found.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(Long id) {
        super(String.format("Ticket with ID %d not found", id));
    }
}
