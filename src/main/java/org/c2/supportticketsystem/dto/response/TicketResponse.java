package org.c2.supportticketsystem.dto.response;

import org.c2.supportticketsystem.model.enums.Priority;
import org.c2.supportticketsystem.model.enums.Status;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for a ticket.
 */
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private String assignee;
    private List<CommentResponse> comments;
    private Instant createdAt;
    private Instant updatedAt;

    public TicketResponse() {
    }

    public TicketResponse(Long id, String title, String description, Priority priority, Status status,
                          String assignee, List<CommentResponse> comments, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignee = assignee;
        this.comments = comments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
