package org.c2.supportticketsystem.dto.response;

import java.time.Instant;

/**
 * Response DTO for a comment.
 */
public class CommentResponse {

    private Long id;
    private Long ticketId;
    private String text;
    private String author;
    private Instant createdAt;

    public CommentResponse() {
    }

    public CommentResponse(Long id, Long ticketId, String text, String author, Instant createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
