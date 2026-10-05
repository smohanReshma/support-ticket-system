package org.c2.supportticketsystem.service;

import org.c2.supportticketsystem.exception.InvalidStateTransitionException;
import org.c2.supportticketsystem.exception.TicketNotFoundException;
import org.c2.supportticketsystem.mapper.CommentMapper;
import org.c2.supportticketsystem.mapper.TicketMapper;
import org.c2.supportticketsystem.model.Comment;
import org.c2.supportticketsystem.model.Ticket;
import org.c2.supportticketsystem.model.enums.Priority;
import org.c2.supportticketsystem.model.enums.Status;
import org.c2.supportticketsystem.repository.CommentRepository;
import org.c2.supportticketsystem.repository.TicketRepository;
import org.c2.supportticketsystem.dto.request.CreateTicketRequest;
import org.c2.supportticketsystem.dto.request.UpdateTicketRequest;
import org.c2.supportticketsystem.dto.response.TicketResponse;
import org.c2.supportticketsystem.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing tickets.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final StateMachineService stateMachineService;
    private final TicketMapper ticketMapper;
    private final CommentMapper commentMapper;

    public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository, 
                         StateMachineService stateMachineService, TicketMapper ticketMapper, 
                         CommentMapper commentMapper) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.stateMachineService = stateMachineService;
        this.ticketMapper = ticketMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * Create a new ticket.
     *
     * @param request the create ticket request
     * @return the created ticket response
     */
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = ticketMapper.toEntity(request);
        ticket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(ticket);
    }

    /**
     * Get a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket response
     * @throws TicketNotFoundException if ticket not found
     */
    public TicketResponse getTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        TicketResponse response = ticketMapper.toResponse(ticket);
        
        List<Comment> comments = commentRepository.findByTicketId(id, Sort.by(Sort.Direction.ASC, "createdAt"));
        response.setComments(comments.stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    /**
     * Get all tickets with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated list of ticket responses sorted by createdAt DESC, then id DESC
     */
    public Page<TicketResponse> getTickets(Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findAll(pageable);
        return tickets.map(ticketMapper::toResponse);
    }

    /**
     * Partially update a ticket.
     * Status cannot be updated through this method.
     *
     * @param id          the ticket ID
     * @param request     the update request
     * @return the updated ticket response
     * @throws TicketNotFoundException if ticket not found
     */
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        ticketMapper.updateEntityFromRequest(ticket, request);
        ticket = ticketRepository.save(ticket);

        TicketResponse response = ticketMapper.toResponse(ticket);
        
        List<Comment> comments = commentRepository.findByTicketId(id, Sort.by(Sort.Direction.ASC, "createdAt"));
        response.setComments(comments.stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    /**
     * Search tickets by keyword across title and description.
     *
     * @param keyword the search keyword (max 100 characters)
     * @param pageable pagination parameters
     * @return paginated list of matching ticket responses
     */
    public Page<TicketResponse> searchTickets(String keyword, Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.search(keyword, pageable);
        return tickets.map(ticketMapper::toResponse);
    }

    /**
     * Filter tickets by status.
     *
     * @param status   the status to filter by
     * @param pageable pagination parameters
     * @return paginated list of tickets with the specified status
     */
    public Page<TicketResponse> filterTickets(Status status, Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findByStatus(status, pageable);
        return tickets.map(ticketMapper::toResponse);
    }

    /**
     * Search and filter tickets simultaneously.
     *
     * @param keyword  the search keyword (max 100 characters)
     * @param status   the status to filter by (optional, may be null)
     * @param pageable pagination parameters
     * @return paginated list of matching ticket responses
     */
    public Page<TicketResponse> searchAndFilterTickets(String keyword, Status status, Pageable pageable) {
        Page<Ticket> tickets;
        if (status != null) {
            tickets = ticketRepository.findByStatusAndSearch(status, keyword, pageable);
        } else {
            tickets = ticketRepository.search(keyword, pageable);
        }
        return tickets.map(ticketMapper::toResponse);
    }

    /**
     * Transition a ticket to a new status.
     *
     * @param id          the ticket ID
     * @param targetStatus the target status
     * @return the updated ticket response
     * @throws TicketNotFoundException if ticket not found
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    public TicketResponse transitionStatus(Long id, Status targetStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (!stateMachineService.isValidTransition(ticket.getStatus(), targetStatus)) {
            throw new InvalidStateTransitionException(
                    ticket.getStatus(),
                    targetStatus,
                    stateMachineService.getValidTransitions(ticket.getStatus())
            );
        }

        ticket.setStatus(targetStatus);
        ticket.setUpdatedAt(Instant.now());
        ticket = ticketRepository.save(ticket);

        TicketResponse response = ticketMapper.toResponse(ticket);
        
        List<Comment> comments = commentRepository.findByTicketId(id, Sort.by(Sort.Direction.ASC, "createdAt"));
        response.setComments(comments.stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    /**
     * Get comments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return list of comment responses sorted by createdAt ASC
     */
    public List<CommentResponse> getComments(Long ticketId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        return commentRepository.findByTicketId(ticketId, sort).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
