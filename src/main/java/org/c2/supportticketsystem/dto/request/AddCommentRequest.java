package org.c2.supportticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding a comment to a ticket.
 */
public class AddCommentRequest {

    @NotBlank
    @Size(max = 5000)
    private String text;

    @NotBlank
    @Size(max = 255)
    private String author;

    public AddCommentRequest() {
    }

    public AddCommentRequest(String text, String author) {
        this.text = text;
        this.author = author;
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
}
