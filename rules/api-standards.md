# REST API Standards

## Endpoint Naming

### Resources
- Use plural nouns: `/tickets`, `/comments`
- Use hyphen-case for multi-word paths: `/ticket-comments`

### Versioning
- Include version in path: `/api/v1/tickets`
- Support backward compatibility when possible

### Path Parameters
- Use hyphen-case: `/tickets/{ticket-id}`
- Use descriptive names: `/users/{user-id}/tickets`

## HTTP Methods

| Method | Usage | Idempotent | Safe |
|--------|-------|------------|------|
| GET | Retrieve resources | Yes | Yes |
| POST | Create resources | No | No |
| PUT | Update resources (full) | Yes | No |
| PATCH | Update resources (partial) | Yes | No |
| DELETE | Remove resources | Yes | No |

## Request/Response Format

### Content-Type
- JSON format for all requests and responses
- `Content-Type: application/json` header on requests
- `Accept: application/json` header on requests

### Field Naming
- Use camelCase for all JSON fields
- Consistent naming across all endpoints

### Response Structure

#### Success Response
```json
{
  "id": 1,
  "title": "Issue with login",
  "description": "Cannot log in with valid credentials",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": {
    "id": 5,
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:45:00Z"
}
```

#### Collection Response
```json
{
  "content": [
    {
      "id": 1,
      "title": "Issue with login",
      "status": "OPEN",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": 2,
      "title": "Feature request",
      "status": "IN_PROGRESS",
      "priority": "MEDIUM",
      "createdAt": "2024-01-14T09:15:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

## Validation

### Request Validation
- Use `@Valid` or `@Validated` on `@RequestBody`
- Use Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.)
- Return `400 Bad Request` with detailed validation errors

### Validation Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tickets",
  "violations": [
    {
      "field": "title",
      "message": "must not be blank"
    },
    {
      "field": "priority",
      "message": "must be one of: LOW, MEDIUM, HIGH, URGENT"
    }
  ]
}
```

## Consistent Error Response

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket with ID 999 not found",
  "path": "/api/v1/tickets/999"
}
```

### HTTP Status Codes

| Code | Use Case | Example |
|------|----------|---------|
| 200 | Success (GET, PUT, PATCH) | Resource retrieved/updated |
| 201 | Created (POST) | Resource created successfully |
| 400 | Bad Request | Validation failed, invalid input |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Invalid state transition, duplicate resource |
| 422 | Unprocessable Entity | Semantic validation error |
| 500 | Internal Server Error | Unexpected server error |

## Search and Filter Conventions

### Search
- Use `?search={keyword}` for keyword search
- Search across multiple fields if applicable
- Return partial matches

```
GET /api/v1/tickets?search=login
```

### Filter
- Use query parameters for filters
- Support multiple filters
- Use descriptive filter names

```
GET /api/v1/tickets?status=OPEN&priority=HIGH
GET /api/v1/tickets?assigneeId=5
```

### Pagination
- Use `?page={number}&size={size}` for pagination
- Default page: 0, default size: 20
- Include pagination metadata in response

```
GET /api/v1/tickets?page=0&size=10
```

### Sort
- Use `?sort={field},{direction}` for sorting
- Default direction: asc

```
GET /api/v1/tickets?sort=createdAt,desc
GET /api/v1/tickets?sort=priority,asc&sort=createdAt,desc
```

## DTO Usage

### Never expose entities directly
- Create separate DTO classes for API contracts
- Entities are persistence concerns
- DTOs are API contracts
- Allows for versioning flexibility

### Request DTOs
- Create specific DTOs for create/update operations
- Use validation annotations
- May include only required fields

```java
public class CreateTicketRequest {
    @NotBlank
    private String title;
    
    @NotBlank
    private String description;
    
    @NotNull
    private Priority priority;
    
    private Long assigneeId;
}
```

### Response DTOs
- Include all fields needed by the client
- May include derived/computed fields
- Can flatten nested relationships

```java
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private UserSummary assignee;
    private List<CommentSummary> comments;
    private Instant createdAt;
    private Instant updatedAt;
}
```

## Status Transition Operations

### Dedicated Status-Transition Endpoint
- Use a dedicated endpoint for status changes
- Never allow direct status modification via PATCH/PUT
- State machine logic enforced in this endpoint

```
POST /api/v1/tickets/{ticket-id}/transition
Content-Type: application/json

{
  "targetStatus": "IN_PROGRESS"
}
```

### State Machine Enforcement
- Backend must enforce all state transitions
- Reject invalid transitions with `409 Conflict`
- Include current state in response for debugging

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Invalid state transition: OPEN -> RESOLVED. Valid transitions from OPEN: [IN_PROGRESS, CANCELLED]",
  "currentStatus": "OPEN",
  "requestedStatus": "RESOLVED",
  "validTransitions": ["IN_PROGRESS", "CANCELLED"],
  "path": "/api/v1/tickets/1/transition"
}
```

### State Transition Validation
- Always validate state machine rules
- Never allow bypassing the state machine
- Log state transitions for audit trail
- Include user context in validation (if applicable)