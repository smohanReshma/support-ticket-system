package org.c2.supportticketsystem.controller;

import org.c2.supportticketsystem.dto.request.CreateTicketRequest;
import org.c2.supportticketsystem.dto.request.UpdateTicketRequest;
import org.c2.supportticketsystem.dto.request.TransitionRequest;
import org.c2.supportticketsystem.dto.response.TicketResponse;
import org.c2.supportticketsystem.dto.response.TicketListResponse;
import org.c2.supportticketsystem.dto.response.CommentResponse;
import org.c2.supportticketsystem.model.enums.Status;
import org.c2.supportticketsystem.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing tickets.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Create a new ticket.
     *
     * @param request the create ticket request
     * @return the created ticket response with 201 Created
     */
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tickets with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated list of tickets
     */
    @GetMapping
    public ResponseEntity<TicketListResponse> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Status status) {

        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<TicketResponse> ticketPage;
        if (search != null && !search.isEmpty()) {
            ticketPage = ticketService.searchAndFilterTickets(search, status, pageable);
        } else if (status != null) {
            ticketPage = ticketService.filterTickets(status, pageable);
        } else {
            ticketPage = ticketService.getTickets(pageable);
        }

        TicketListResponse response = new TicketListResponse();
        response.setContent(ticketPage.getContent());
        response.setPage(ticketPage.getNumber());
        response.setSize(ticketPage.getSize());
        response.setTotalElements(ticketPage.getTotalElements());
        response.setTotalPages(ticketPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket response
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        TicketResponse response = ticketService.getTicket(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update a ticket.
     *
     * @param id      the ticket ID
     * @param request the update request
     * @return the updated ticket response
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long id, 
                                                        @Valid @RequestBody UpdateTicketRequest request) {
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Transition a ticket to a new status.
     *
     * @param id          the ticket ID
     * @param request     the transition request
     * @return the updated ticket response
     */
    @PostMapping("/{id}/transition")
    public ResponseEntity<TicketResponse> transitionStatus(@PathVariable Long id,
                                                           @Valid @RequestBody TransitionRequest request) {
        TicketResponse response = ticketService.transitionStatus(id, request.getTargetStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Get comments for a ticket.
     *
     * @param id the ticket ID
     * @return list of comment responses
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getTicketComments(@PathVariable Long id) {
        List<CommentResponse> comments = ticketService.getComments(id);
        return ResponseEntity.ok(comments);
    }
}
