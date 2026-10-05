package org.c2.supportticketsystem.dto.request;

import jakarta.validation.constraints.Size;

import org.c2.supportticketsystem.model.enums.Priority;

/**
 * Request DTO for partially updating a ticket.
 * Status cannot be updated through this request.
 */
public class UpdateTicketRequest {

    @Size(max = 255)
    private String title;

    @Size(max = 10000)
    private String description;

    private Priority priority;

    @Size(max = 255)
    private String assignee;

    public UpdateTicketRequest() {
    }

    public UpdateTicketRequest(String title, String description, Priority priority, String assignee) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
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

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
