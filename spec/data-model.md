# Support Ticket Management System - Data Model

## 1. Overview

This document defines the database schema and entity relationships for the Support Ticket Management System. The data model is implemented using JPA/Hibernate with PostgreSQL as the primary database and H2 for development/testing.

## 2. Entities

### 2.1 Ticket Entity

Represents a support ticket in the system.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| title | String | Not null, Max 255 chars | Brief description of the issue |
| description | String | Not null, Max 10,000 chars | Detailed description |
| priority | Enum (LOW, MEDIUM, HIGH, URGENT) | Not null | Priority level |
| status | Enum (OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED) | Not null, Default: OPEN | Current state |
| assignee | String | Max 255 chars, Nullable | Identifier for assignee |
| createdAt | Instant | Not null | Creation timestamp |
| updatedAt | Instant | Not null | Last update timestamp |

#### JPA Annotations
```java
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @NotBlank
    @Column(name = "description", nullable = false, length = 10000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.OPEN;
    
    @Column(name = "assignee", length = 255)
    private String assignee;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

#### Database Schema
```sql
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED', 'CLOSED')),
    assignee VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_assignee ON tickets(assignee);
```

### 2.2 Comment Entity

Represents a comment associated with a ticket.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| ticketId | Long | Foreign Key, Not null | Reference to ticket |
| text | String | Not null, Max 5,000 chars | Comment content |
| author | String | Not null, Max 255 chars | Comment author |
| createdAt | Instant | Not null | Creation timestamp |

#### JPA Annotations
```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;
    
    @NotBlank
    @Column(name = "text", nullable = false, length = 5000)
    private String text;
    
    @NotBlank
    @Column(name = "author", nullable = false, length = 255)
    private String author;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
```

#### Database Schema
```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    text VARCHAR(5000) NOT NULL,
    author VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_comments_ticket_id ON comments(ticket_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);
```

## 3. Relationships

### 3.1 Ticket to Comments

**Relationship:** One-to-Many

A ticket can have zero or more comments. Comments are deleted when their associated ticket is deleted (cascade).

```java
// In Ticket entity
@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments = new ArrayList<>();
```

## 4. Enumerations

### 4.1 Priority Enum

Valid priority values for tickets.

| Value | Description |
|-------|-------------|
| LOW | Low priority issue |
| MEDIUM | Medium priority issue |
| HIGH | High priority issue |
| URGENT | Urgent priority issue |

### 4.2 Status Enum

Valid status values for tickets (state machine).

| Value | Description |
|-------|-------------|
| OPEN | New ticket, not yet worked on |
| IN_PROGRESS | Ticket is being worked on |
| RESOLVED | Issue has been resolved |
| CANCELLED | Ticket was cancelled |
| CLOSED | Resolved ticket has been closed |

## 5. Timestamp Behavior

### 5.1 Automatic Timestamp Management

- `createdAt`: Set when the entity is first created, never updated
- `updatedAt`: Set when the entity is created, updated on every modification

### 5.2 JPA Auditing

```java
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditableEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

## 6. Indexes

### 6.1 Performance Indexes

| Index Name | Table | Columns | Purpose |
|------------|-------|---------|---------|
| idx_tickets_created_at | tickets | created_at DESC | Sort tickets by creation time |
| idx_tickets_status | tickets | status | Filter by status |
| idx_tickets_assignee | tickets | assignee | Filter by assignee |
| idx_comments_ticket_id | comments | ticket_id | Find comments for a ticket |
| idx_comments_created_at | comments | created_at | Sort comments chronologically |

## 7. Constraints

### 7.1 Database Constraints

```sql
-- Ticket constraints
ALTER TABLE tickets 
ADD CONSTRAINT chk_ticket_priority 
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

ALTER TABLE tickets 
ADD CONSTRAINT chk_ticket_status 
CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED', 'CLOSED'));

-- Comment constraints
ALTER TABLE comments 
ADD CONSTRAINT fk_comment_ticket 
FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE;
```

### 7.2 Validation Constraints

| Field | Constraint | Message |
|-------|-----------|---------|
| title | @NotBlank | must not be blank |
| title | @Size(max = 255) | must be no more than 255 characters |
| description | @NotBlank | must not be blank |
| description | @Size(max = 10000) | must be no more than 10000 characters |
| priority | @NotNull | must not be null |
| assignee | @Size(max = 255) | must be no more than 255 characters |
| text | @NotBlank | must not be blank |
| text | @Size(max = 5000) | must be no more than 5000 characters |
| author | @NotBlank | must not be blank |
| author | @Size(max = 255) | must be no more than 255 characters |

## 8. Repository Methods

### 8.1 TicketRepository

```java
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Find by status
    List<Ticket> findByStatus(Status status, Sort sort);
    
    // Search by title or description (case-insensitive)
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> search(String keyword, Sort sort);
    
    // Find by status with search
    @Query("SELECT t FROM Ticket t WHERE " +
           "t.status = :status AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Ticket> findByStatusAndSearch(String status, String keyword, Sort sort);
    
    // Pagination support
    Page<Ticket> findAll(Sort sort, Pageable pageable);
}
```

### 8.2 CommentRepository

```java
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Find comments by ticket ID
    List<Comment> findByTicketId(Long ticketId, Sort sort);
    
    // Count comments by ticket ID
    long countByTicketId(Long ticketId);
}
```

## 9. Database Migrations

For production, use Flyway or Liquibase for schema versioning:

```
src/main/resources/db/migration/
└── V1__create_initial_schema.sql
```

Example migration file:
```sql
-- Create tables
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED', 'CLOSED')),
    assignee VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    text VARCHAR(5000) NOT NULL,
    author VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create indexes
CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX idx_tickets_status ON tickets(status);

-- Enable H2-specific features for dev (optional)
-- CREATE ALIAS IF NOT EXISTS SLEEP FOR "org.h2.util.SLEEP.sleep";
```

## 10. Transactional Operations

### 10.1 Required Transactional Scenarios

| Operation | Transaction | Isolation |
|-----------|-------------|-----------|
| Create ticket | Read/Write | READ_COMMITTED |
| Update ticket | Read/Write | READ_COMMITTED |
| State transition | Read/Write | READ_COMMITTED |
| Add comment | Read/Write | READ_COMMITTED |
| List tickets | Read-only | READ_COMMITTED |
| Search tickets | Read-only | READ_COMMITTED |

### 10.2 Transaction Example

```java
@Service
@Transactional
public class TicketService {
    
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setAssignee(request.getAssignee());
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        
        ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public TicketResponse transitionStatus(Long ticketId, Status newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        // State machine validation
        if (!isValidTransition(ticket.getStatus(), newStatus)) {
            throw new InvalidStateTransitionException(
                ticket.getStatus(), newStatus, getValidTransitions(ticket.getStatus()));
        }
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(Instant.now());
        
        ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
}
```

## 11. Data Integrity

### 11.1 Enforced Constraints

- Primary key constraints on all entities
- Foreign key constraints on comments (on delete cascade)
- Check constraints for enum values
- NOT NULL constraints on required fields
- Unique constraints where applicable

### 11.2 Business Rule Integrity

- State machine transitions enforced in service layer (not database)
- Validation errors returned before persistence
- Transaction rollback on any validation failure
