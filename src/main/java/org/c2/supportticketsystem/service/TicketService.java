package org.c2.supportticketsystem.service;

import org.c2.supportticketsystem.exception.InvalidStateTransitionException;
import org.c2.supportticketsystem.exception.TicketNotFoundException;
import org.c2.supportticketsystem.model.Comment;
import org.c2.supportticketsystem.model.Ticket;
import org.c2.supportticketsystem.model.enums.Priority;
import org.c2.supportticketsystem.model.enums.Status;
import org.c2.supportticketsystem.repository.CommentRepository;
import org.c2.supportticketsystem.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing tickets.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final StateMachineService stateMachineService;

    public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository, StateMachineService stateMachineService) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.stateMachineService = stateMachineService;
    }

    /**
     * Create a new ticket.
     *
     * @param title       the ticket title
     * @param description the ticket description
     * @param priority    the ticket priority
     * @param assignee    the assignee identifier (optional, may be null)
     * @return the created ticket
     */
    public Ticket createTicket(String title, String description, Priority priority, String assignee) {
        Ticket ticket = new Ticket(title, description, priority, assignee);
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticketRepository.save(ticket);
    }

    /**
     * Get a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket
     * @throws TicketNotFoundException if ticket not found
     */
    public Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    /**
     * Get all tickets with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated list of tickets sorted by createdAt DESC, then id DESC
     */
    public Page<Ticket> getTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    /**
     * Partially update a ticket.
     * Status cannot be updated through this method.
     *
     * @param id          the ticket ID
     * @param title       new title (optional, may be null)
     * @param description new description (optional, may be null)
     * @param priority    new priority (optional, may be null)
     * @param assignee    new assignee (optional, may be null to clear)
     * @return the updated ticket
     * @throws TicketNotFoundException if ticket not found
     */
    public Ticket updateTicket(Long id, String title, String description, Priority priority, String assignee) {
        Ticket ticket = getTicket(id);

        if (title != null && !title.isBlank()) {
            ticket.setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            ticket.setDescription(description);
        }
        if (priority != null) {
            ticket.setPriority(priority);
        }
        if (assignee != null) {
            ticket.setAssignee(assignee);
        }

        ticket.setUpdatedAt(Instant.now());
        return ticketRepository.save(ticket);
    }

    /**
     * Search tickets by keyword across title and description.
     *
     * @param keyword the search keyword (max 100 characters)
     * @param pageable pagination parameters
     * @return paginated list of matching tickets sorted by createdAt DESC, then id DESC
     */
    public Page<Ticket> searchTickets(String keyword, Pageable pageable) {
        return ticketRepository.search(keyword, pageable);
    }

    /**
     * Filter tickets by status.
     *
     * @param status   the status to filter by
     * @param pageable pagination parameters
     * @return paginated list of tickets with the specified status
     */
    public Page<Ticket> filterTickets(Status status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }

    /**
     * Search and filter tickets simultaneously.
     *
     * @param keyword  the search keyword (max 100 characters)
     * @param status   the status to filter by (optional, may be null)
     * @param pageable pagination parameters
     * @return paginated list of matching tickets
     */
    public Page<Ticket> searchAndFilterTickets(String keyword, Status status, Pageable pageable) {
        if (status != null) {
            return ticketRepository.findByStatusAndSearch(status, keyword, pageable);
        }
        return ticketRepository.search(keyword, pageable);
    }

    /**
     * Transition a ticket to a new status.
     *
     * @param id          the ticket ID
     * @param targetStatus the target status
     * @return the updated ticket
     * @throws TicketNotFoundException if ticket not found
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    public Ticket transitionStatus(Long id, Status targetStatus) {
        Ticket ticket = getTicket(id);

        if (!stateMachineService.isValidTransition(ticket.getStatus(), targetStatus)) {
            throw new InvalidStateTransitionException(
                    ticket.getStatus(),
                    targetStatus,
                    stateMachineService.getValidTransitions(ticket.getStatus())
            );
        }

        ticket.setStatus(targetStatus);
        ticket.setUpdatedAt(Instant.now());
        return ticketRepository.save(ticket);
    }

    /**
     * Get comments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return list of comments sorted by createdAt ASC
     */
    public List<Comment> getComments(Long ticketId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        return commentRepository.findByTicketId(ticketId, sort);
    }
}
