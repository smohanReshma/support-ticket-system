package org.c2.supportticketsystem.exception;

import org.c2.supportticketsystem.model.enums.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;

/**
 * Global exception handler for the application.
 * Catches all exceptions and returns structured error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation exceptions.
     *
     * @param ex the validation exception
     * @param request the web request
     * @return error response with 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new Violation(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setMessage("Validation failed");
        error.setPath(request.getDescription(false));
        error.setViolations(violations);

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle ticket not found exceptions.
     *
     * @param ex the ticket not found exception
     * @return error response with 404 Not Found
     */
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketNotFoundException(
            TicketNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getDescription(false));

        return ResponseEntity.notFound().build();
    }

    /**
     * Handle comment not found exceptions.
     *
     * @param ex the comment not found exception
     * @return error response with 404 Not Found
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFoundException(
            CommentNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getDescription(false));

        return ResponseEntity.notFound().build();
    }

    /**
     * Handle invalid state transition exceptions.
     *
     * @param ex the invalid state transition exception
     * @return error response with 409 Conflict
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(
            InvalidStateTransitionException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setError(HttpStatus.CONFLICT.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getDescription(false));
        error.setCurrentStatus(ex.getCurrentStatus().toString());
        error.setRequestedStatus(ex.getRequestedStatus().toString());
        error.setValidTransitions(
                ex.getValidTransitions().stream()
                        .map(Status::toString)
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMessage("An unexpected error occurred");
        error.setPath(request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Internal class representing a validation violation.
     */
    public static class Violation {
        private String field;
        private String message;

        public Violation() {
        }

        public Violation(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Internal class representing an error response.
     */
    public static class ErrorResponse {
        private Instant timestamp;
        private Integer status;
        private String error;
        private String message;
        private String path;
        private List<Violation> violations;
        private String currentStatus;
        private String requestedStatus;
        private List<String> validTransitions;

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<Violation> getViolations() {
            return violations;
        }

        public void setViolations(List<Violation> violations) {
            this.violations = violations;
        }

        public String getCurrentStatus() {
            return currentStatus;
        }

        public void setCurrentStatus(String currentStatus) {
            this.currentStatus = currentStatus;
        }

        public String getRequestedStatus() {
            return requestedStatus;
        }

        public void setRequestedStatus(String requestedStatus) {
            this.requestedStatus = requestedStatus;
        }

        public List<String> getValidTransitions() {
            return validTransitions;
        }

        public void setValidTransitions(List<String> validTransitions) {
            this.validTransitions = validTransitions;
        }
    }
}
