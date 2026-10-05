package org.c2.supportticketsystem.controller;

import org.c2.supportticketsystem.dto.request.AddCommentRequest;
import org.c2.supportticketsystem.dto.response.CommentResponse;
import org.c2.supportticketsystem.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing comments.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Add a comment to a ticket.
     *
     * @param ticketId the ticket ID
     * @param request  the add comment request
     * @return the created comment response with 201 Created
     */
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long ticketId,
                                                      @Valid @RequestBody AddCommentRequest request) {
        CommentResponse response = commentService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a comment by ID.
     *
     * @param id the comment ID
     * @return the comment response
     */
    @GetMapping("/{ticketId}/comments/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long id) {
        CommentResponse response = commentService.getComment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all comments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return list of comment responses
     */
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long ticketId) {
        List<CommentResponse> comments = commentService.getComments(ticketId);
        return ResponseEntity.ok(comments);
    }
}
