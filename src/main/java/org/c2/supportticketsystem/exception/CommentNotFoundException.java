package org.c2.supportticketsystem.exception;

/**
 * Exception thrown when a comment is not found.
 */
public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super(String.format("Comment with ID %d not found", id));
    }
}
