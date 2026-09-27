# Support Ticket Management System - State Machine

## 1. Overview

This document defines the state machine for ticket status transitions in the Support Ticket Management System. The state machine is enforced by the backend service layer to ensure data integrity and business rule compliance.

## 2. State Definitions

### 2.1 Valid Status Values

| Status | Description |
|--------|-------------|
| OPEN | New ticket, not yet worked on |
| IN_PROGRESS | Ticket is being worked on |
| RESOLVED | Issue has been resolved |
| CANCELLED | Ticket was cancelled |
| CLOSED | Resolved ticket has been closed |

### 2.2 Initial State

All new tickets start with status **OPEN**.

## 3. Valid Transitions

### 3.1 From OPEN

| Current | Next | Description |
|---------|------|-------------|
| OPEN | IN_PROGRESS | Open ticket can be started |
| OPEN | CANCELLED | Open ticket can be cancelled |

### 3.2 From IN_PROGRESS

| Current | Next | Description |
|---------|------|-------------|
| IN_PROGRESS | RESOLVED | In-progress ticket can be resolved |
| IN_PROGRESS | CANCELLED | In-progress ticket can be cancelled |

### 3.3 From RESOLVED

| Current | Next | Description |
|---------|------|-------------|
| RESOLVED | CLOSED | Resolved ticket can be closed |

### 3.4 From CANCELLED

| Current | Next | Description |
|---------|------|-------------|
| None | None | No valid transitions from cancelled |

### 3.5 From CLOSED

| Current | Next | Description |
|---------|------|-------------|
| None | None | No valid transitions from closed |

## 4. Invalid Transitions

All transitions not listed in Section 3 are considered invalid and will return **HTTP 409 Conflict**.

### 4.1 Explicit Invalid Transitions Table

| From | To | HTTP Response | Reason |
|------|----|---------------|--------|
| OPEN | RESOLVED | 409 Conflict | Invalid: bypasses IN_PROGRESS |
| OPEN | CLOSED | 409 Conflict | Invalid: must go through RESOLVED |
| OPEN | OPEN | 409 Conflict | Invalid: same state |
| IN_PROGRESS | OPEN | 409 Conflict | Invalid: backwards transition |
| IN_PROGRESS | IN_PROGRESS | 409 Conflict | Invalid: same state |
| RESOLVED | OPEN | 409 Conflict | Invalid: backwards transition |
| RESOLVED | IN_PROGRESS | 409 Conflict | Invalid: backwards transition |
| RESOLVED | RESOLVED | 409 Conflict | Invalid: same state |
| RESOLVED | CANCELLED | 409 Conflict | Invalid: cannot transition from resolved |
| CANCELLED | OPEN | 409 Conflict | Invalid: cannot restart cancelled |
| CANCELLED | IN_PROGRESS | 409 Conflict | Invalid: cannot restart cancelled |
| CANCELLED | RESOLVED | 409 Conflict | Invalid: cannot restart cancelled |
| CANCELLED | CANCELLED | 409 Conflict | Invalid: same state |
| CLOSED | OPEN | 409 Conflict | Invalid: cannot restart closed |
| CLOSED | IN_PROGRESS | 409 Conflict | Invalid: cannot restart closed |
| CLOSED | RESOLVED | 409 Conflict | Invalid: cannot restart closed |
| CLOSED | CANCELLED | 409 Conflict | Invalid: cannot restart closed |
| CLOSED | CLOSED | 409 Conflict | Invalid: same state |

## 5. State Transition Endpoint

### 5.1 Request

**Endpoint:** `POST /api/v1/tickets/{id}/transition`

**Request Body:**
```json
{
  "targetStatus": "IN_PROGRESS"
}
```

### 5.2 Response

#### Success (200 OK)
```json
{
  "id": 1,
  "title": "Issue with login",
  "description": "Cannot log in with valid credentials",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignee": "john.doe@example.com",
  "comments": [],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T12:30:00Z"
}
```

#### Invalid Status Value (400 Bad Request)
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tickets/1/transition",
  "violations": [
    {
      "field": "targetStatus",
      "message": "must not be null"
    }
  ]
}
```

#### Resource Not Found (404 Not Found)
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket with ID 999 not found",
  "path": "/api/v1/tickets/999/transition"
}
```

#### Invalid Transition (409 Conflict)
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Invalid state transition: OPEN -> RESOLVED",
  "path": "/api/v1/tickets/1/transition",
  "currentStatus": "OPEN",
  "requestedStatus": "RESOLVED",
  "validTransitions": ["IN_PROGRESS", "CANCELLED"]
}
```

## 6. Error Response Details

### 6.1 Error Response Fields

| Field | Type | Description |
|-------|------|-------------|
| timestamp | string | ISO 8601 timestamp |
| status | integer | HTTP status code |
| error | string | HTTP error name |
| message | string | Human-readable error message |
| path | string | Request path |
| currentStatus | string (optional) | Current ticket status |
| requestedStatus | string (optional) | Requested status value |
| validTransitions | array (optional) | List of valid next states |

### 6.2 Validation Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tickets/1/transition",
  "violations": [
    {
      "field": "targetStatus",
      "message": "must not be null"
    }
  ]
}
```

### 6.3 Invalid State Transition Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Invalid state transition: OPEN -> RESOLVED",
  "path": "/api/v1/tickets/1/transition",
  "currentStatus": "OPEN",
  "requestedStatus": "RESOLVED",
  "validTransitions": ["IN_PROGRESS", "CANCELLED"]
}
```

## 7. State Machine Implementation

### 7.1 Service Layer Enforcement

The state machine is enforced in the `StateTransitionService` class:

```java
@Service
@Transactional
public class StateTransitionService {
    
    public Ticket transitionStatus(Long ticketId, Status newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        if (!isValidTransition(ticket.getStatus(), newStatus)) {
            throw new InvalidStateTransitionException(
                ticket.getStatus(), 
                newStatus,
                getValidTransitions(ticket.getStatus())
            );
        }
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(Instant.now());
        
        return ticketRepository.save(ticket);
    }
    
    private boolean isValidTransition(Status current, Status target) {
        switch (current) {
            case OPEN -> {
                return target == Status.IN_PROGRESS || target == Status.CANCELLED;
            }
            case IN_PROGRESS -> {
                return target == Status.RESOLVED || target == Status.CANCELLED;
            }
            case RESOLVED -> {
                return target == Status.CLOSED;
            }
            case CANCELLED, CLOSED -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }
    
    private List<Status> getValidTransitions(Status current) {
        switch (current) {
            case OPEN -> {
                return List.of(Status.IN_PROGRESS, Status.CANCELLED);
            }
            case IN_PROGRESS -> {
                return List.of(Status.RESOLVED, Status.CANCELLED);
            }
            case RESOLVED -> {
                return List.of(Status.CLOSED);
            }
            case CANCELLED, CLOSED -> {
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }
}
```

### 7.2 Controller Layer

```java
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {
    
    @PostMapping("/{id}/transition")
    public ResponseEntity<TicketResponse> transitionStatus(
        @PathVariable Long id,
        @Valid @RequestBody TransitionRequest request
    ) {
        Ticket updatedTicket = stateTransitionService.transitionStatus(id, request.getTargetStatus());
        return ResponseEntity.ok(ticketMapper.toResponse(updatedTicket));
    }
}
```

## 8. Transactional Behavior

### 8.1 Transaction Boundaries

State transitions are wrapped in `@Transactional` to ensure:

1. Atomicity: Either the entire transition succeeds or fails
2. Consistency: Database constraints are enforced
3. Isolation: Concurrent transitions are handled safely

### 8.2 Concurrency Handling

If two requests attempt to transition the same ticket simultaneously:

1. First request acquires lock and succeeds
2. Second request either waits or fails with optimistic lock exception
3. Client should retry failed requests

## 9. Audit Trail

### 9.1 Timestamp Updates

- `createdAt`: Set once when ticket is created, never changes
- `updatedAt`: Updated on every successful state transition

### 9.2 Persistence

All state changes are persisted to the database before the response is sent to the client.

## 10. Business Rules

### 10.1 Mandatory State Machine

- Frontend cannot be trusted for state transitions
- All state changes must go through the backend
- Direct status modification via PATCH/PUT is blocked

### 10.2 State Sequence Requirements

- OPEN must go through IN_PROGRESS before RESOLVED
- RESOLVED must go to CLOSED
- CANCELLED and CLOSED are terminal states

### 10.3 Error Handling

- Invalid state transitions return 409 Conflict with helpful error message
- Error message includes current state and list of valid next states
- Frontend can use this information to guide user actions

## 11. State Transition Flow Diagram

```
                    ┌─────────┐
                    │   OPEN  │
                    └────┬────┘
                         │
            ┌────────────┴────────────┐
            │                         │
            ▼                         ▼
    ┌──────────────┐          ┌──────────────┐
    │ IN_PROGRESS  │          │ CANCELLED    │
    └──────┬───────┘          └──────┬───────┘
           │                          │
           ▼                          │
    ┌──────────────┐                  │
    │  RESOLVED    │                  │
    └──────┬───────┘                  │
           │                          │
           ▼                          │
    ┌──────────────┐                  │
    │   CLOSED     │◄─────────────────┘
    └──────────────┘
```

## 12. Test Coverage

### 12.1 Valid Transitions

- [ ] OPEN -> IN_PROGRESS
- [ ] OPEN -> CANCELLED
- [ ] IN_PROGRESS -> RESOLVED
- [ ] IN_PROGRESS -> CANCELLED
- [ ] RESOLVED -> CLOSED

### 12.2 Invalid Transitions

- [ ] OPEN -> RESOLVED (bypasses IN_PROGRESS)
- [ ] OPEN -> CLOSED (bypasses IN_PROGRESS and RESOLVED)
- [ ] IN_PROGRESS -> OPEN (backwards)
- [ ] RESOLVED -> OPEN (backwards)
- [ ] RESOLVED -> IN_PROGRESS (backwards)
- [ ] CANCELLED -> Any (terminal state)
- [ ] CLOSED -> Any (terminal state)

### 12.3 Error Responses

- [ ] 409 Conflict for invalid transitions
- [ ] Error message includes valid transitions
- [ ] State remains unchanged after invalid transition
