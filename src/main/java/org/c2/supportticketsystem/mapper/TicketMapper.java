package org.c2.supportticketsystem.mapper;

import org.c2.supportticketsystem.dto.request.CreateTicketRequest;
import org.c2.supportticketsystem.dto.request.UpdateTicketRequest;
import org.c2.supportticketsystem.dto.response.TicketResponse;
import org.c2.supportticketsystem.model.Ticket;
import org.c2.supportticketsystem.model.enums.Status;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Mapper for Ticket entity to/from DTOs.
 */
@Component
public class TicketMapper {

    /**
     * Converts a Ticket entity to a TicketResponse DTO.
     *
     * @param ticket the Ticket entity
     * @return the TicketResponse DTO
     */
    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setAssignee(ticket.getAssignee());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        return response;
    }

    /**
     * Converts a CreateTicketRequest DTO to a Ticket entity.
     *
     * @param request the CreateTicketRequest DTO
     * @return the Ticket entity
     */
    public Ticket toEntity(CreateTicketRequest request) {
        if (request == null) {
            return null;
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setAssignee(request.getAssignee());
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }

    /**
     * Updates a Ticket entity with data from an UpdateTicketRequest DTO.
     * Status cannot be updated through this method.
     *
     * @param ticket     the existing Ticket entity
     * @param request    the UpdateTicketRequest DTO
     */
    public void updateEntityFromRequest(Ticket ticket, UpdateTicketRequest request) {
        if (request == null) {
            return;
        }

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getAssignee() != null) {
            ticket.setAssignee(request.getAssignee());
        }
        ticket.setUpdatedAt(Instant.now());
    }
}
