package org.c2.supportticketsystem.service;

import org.c2.supportticketsystem.exception.CommentNotFoundException;
import org.c2.supportticketsystem.exception.TicketNotFoundException;
import org.c2.supportticketsystem.mapper.CommentMapper;
import org.c2.supportticketsystem.mapper.TicketMapper;
import org.c2.supportticketsystem.model.Comment;
import org.c2.supportticketsystem.model.Ticket;
import org.c2.supportticketsystem.repository.CommentRepository;
import org.c2.supportticketsystem.repository.TicketRepository;
import org.c2.supportticketsystem.dto.request.AddCommentRequest;
import org.c2.supportticketsystem.dto.response.CommentResponse;
import org.c2.supportticketsystem.dto.response.TicketResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing comments.
 */
@Service
@Transactional
public class CommentService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final TicketMapper ticketMapper;
    private final CommentMapper commentMapper;

    public CommentService(TicketRepository ticketRepository, CommentRepository commentRepository, 
                          TicketMapper ticketMapper, CommentMapper commentMapper) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.ticketMapper = ticketMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * Add a comment to a ticket.
     *
     * @param ticketId the ticket ID
     * @param request  the add comment request
     * @return the created comment response
     * @throws TicketNotFoundException if ticket not found
     */
    public CommentResponse addComment(Long ticketId, AddCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Comment comment = commentMapper.toEntity(request, ticket);
        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    /**
     * Get comments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return list of comment responses sorted by createdAt ASC
     * @throws TicketNotFoundException if ticket not found
     */
    public List<CommentResponse> getComments(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        return commentRepository.findByTicketId(ticketId, sort).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a comment by ID.
     *
     * @param id the comment ID
     * @return the comment response
     * @throws CommentNotFoundException if comment not found
     */
    public CommentResponse getComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        return commentMapper.toResponse(comment);
    }
}
