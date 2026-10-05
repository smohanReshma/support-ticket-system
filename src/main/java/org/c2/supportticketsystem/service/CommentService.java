package org.c2.supportticketsystem.service;

import org.c2.supportticketsystem.exception.CommentNotFoundException;
import org.c2.supportticketsystem.exception.TicketNotFoundException;
import org.c2.supportticketsystem.model.Comment;
import org.c2.supportticketsystem.model.Ticket;
import org.c2.supportticketsystem.repository.CommentRepository;
import org.c2.supportticketsystem.repository.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing comments.
 */
@Service
@Transactional
public class CommentService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public CommentService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Add a comment to a ticket.
     *
     * @param ticketId the ticket ID
     * @param text     the comment text
     * @param author   the comment author
     * @return the created comment
     * @throws TicketNotFoundException if ticket not found
     */
    public Comment addComment(Long ticketId, String text, String author) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setText(text);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    /**
     * Get comments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return list of comments sorted by createdAt ASC
     * @throws TicketNotFoundException if ticket not found
     */
    public List<Comment> getComments(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        return commentRepository.findByTicketId(ticketId, sort);
    }

    /**
     * Get a comment by ID.
     *
     * @param id the comment ID
     * @return the comment
     * @throws CommentNotFoundException if comment not found
     */
    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }
}
