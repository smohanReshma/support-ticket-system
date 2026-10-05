package org.c2.supportticketsystem.mapper;

import org.c2.supportticketsystem.dto.request.AddCommentRequest;
import org.c2.supportticketsystem.dto.response.CommentResponse;
import org.c2.supportticketsystem.model.Comment;
import org.c2.supportticketsystem.model.Ticket;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper for Comment entity to/from DTOs.
 */
@Component
public class CommentMapper {

    /**
     * Converts a Comment entity to a CommentResponse DTO.
     *
     * @param comment the Comment entity
     * @return the CommentResponse DTO
     */
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setTicketId(comment.getTicket().getId());
        response.setText(comment.getText());
        response.setAuthor(comment.getAuthor());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    /**
     * Converts an AddCommentRequest DTO to a Comment entity.
     *
     * @param request the AddCommentRequest DTO
     * @param ticket  the associated Ticket entity
     * @return the Comment entity
     */
    public Comment toEntity(AddCommentRequest request, Ticket ticket) {
        if (request == null || ticket == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setText(request.getText());
        comment.setAuthor(request.getAuthor());
        comment.setCreatedAt(Instant.now());
        return comment;
    }
}
