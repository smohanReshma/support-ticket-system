# Support Ticket Management System - API Contract

## 1. Overview

This document defines the REST API contract for the Support Ticket Management System. All endpoints follow the standards defined in `rules/api-standards.md`.

## 2. Base Information

| Property | Value |
|----------|-------|
| Base Path | `/api/v1` |
| Version | v1 |
| Content-Type | application/json |
| Accept | application/json |

## 3. Response Formats

### 3.1 Success Response

All successful responses return the appropriate HTTP status code with a JSON body.

### 3.2 Collection Response (Paginated)

```json
{
  "content": [
    {
      "id": 1,
      "title": "Issue with login",
      "status": "OPEN",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## 4. Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tickets"
}
```

### 4.1 Validation Error Response

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

### 4.2 Resource Not Found Response

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket with ID 999 not found",
  "path": "/api/v1/tickets/999"
}
```

### 4.3 Invalid State Transition Response

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

## 5. Ticket Endpoints

### 5.1 Create Ticket

**Endpoint:** `POST /api/v1/tickets`

**Description:** Create a new support ticket.

**Request Body:**
```json
{
  "title": "Issue with login",
  "description": "Cannot log in with valid credentials",
  "priority": "HIGH",
  "assignee": "john.doe@example.com"
}
```

**Request DTO:**
```java
public class CreateTicketRequest {
    @NotBlank
    @Size(max = 255)
    private String title;
    
    @NotBlank
    @Size(max = 10000)
    private String description;
    
    @NotNull
    private Priority priority;
    
    @Size(max = 255)
    private String assignee;
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "title": "Issue with login",
  "description": "Cannot log in with valid credentials",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "john.doe@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- **400 Bad Request:** Validation failed (title blank, description blank, invalid priority)
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.2 List Tickets

**Endpoint:** `GET /api/v1/tickets`

**Description:** Retrieve a paginated list of tickets, sorted by createdAt descending.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | int | No | 0 | Zero-based page number |
| size | int | No | 20 | Number of tickets per page |

**Success Response (200 OK):**
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

**Error Responses:**
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.3 View Ticket Details

**Endpoint:** `GET /api/v1/tickets/{id}`

**Description:** Retrieve detailed information for a specific ticket, including all associated comments.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Ticket ID |

**Success Response (200 OK):**
```json
{
  "id": 1,
  "title": "Issue with login",
  "description": "Cannot log in with valid credentials",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "john.doe@example.com",
  "comments": [
    {
      "id": 1,
      "text": "This is a comment",
      "author": "admin",
      "createdAt": "2024-01-15T11:00:00Z"
    }
  ],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

**Error Responses:**
- **404 Not Found:** Ticket with the given ID does not exist
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.4 Update Ticket (Partial)

**Endpoint:** `PATCH /api/v1/tickets/{id}`

**Description:** Partially update a ticket. Status cannot be updated through this endpoint.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Ticket ID |

**Request Body (all fields optional):**
```json
{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "LOW",
  "assignee": "jane.doe@example.com"
}
```

**Request DTO:**
```java
public class UpdateTicketRequest {
    @Size(max = 255)
    private String title;
    
    @Size(max = 10000)
    private String description;
    
    private Priority priority;
    
    @Size(max = 255)
    private String assignee;
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "title": "Updated title",
  "description": "Updated description",
  "priority": "LOW",
  "status": "OPEN",
  "assignee": "jane.doe@example.com",
  "comments": [],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T12:00:00Z"
}
```

**Error Responses:**
- **400 Bad Request:** Validation failed (invalid field values)
- **404 Not Found:** Ticket with the given ID does not exist
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.5 Search Tickets by Keyword

**Endpoint:** `GET /api/v1/tickets?search={keyword}`

**Description:** Search tickets by keyword across title and description fields.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| search | string | No | Search keyword (max 100 characters) |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Issue with login",
      "status": "OPEN",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**Error Responses:**
- **400 Bad Request:** Search keyword exceeds 100 characters
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.6 Filter Tickets by Status

**Endpoint:** `GET /api/v1/tickets?status={status}`

**Description:** Filter tickets by status.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | string | Yes | Status value: OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Issue with login",
      "status": "OPEN",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**Error Responses:**
- **400 Bad Request:** Invalid status value
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.7 Combined Search and Filter

**Endpoint:** `GET /api/v1/tickets?search={keyword}&status={status}`

**Description:** Combine search and status filter in a single request.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| search | string | No | Search keyword (max 100 characters) |
| status | string | No | Status value |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Login issue",
      "status": "OPEN",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**Error Responses:**
- **400 Bad Request:** Invalid status value or search keyword exceeds 100 characters
- **500 Internal Server Error:** Database error or other unexpected error

---

### 5.8 State Transition

**Endpoint:** `POST /api/v1/tickets/{id}/transition`

**Description:** Transition a ticket to a new status through a dedicated endpoint.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Ticket ID |

**Request Body:**
```json
{
  "targetStatus": "IN_PROGRESS"
}
```

**Request DTO:**
```java
public class TransitionRequest {
    @NotNull
    private Status targetStatus;
}
```

**Success Response (200 OK):**
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

**Error Responses:**
- **400 Bad Request:** targetStatus is missing, malformed, or not a valid status value
- **404 Not Found:** Ticket with the given ID does not exist
- **409 Conflict:** The transition is not allowed by the state machine (see state-machine.md for details)
- **500 Internal Server Error:** Database error or other unexpected error

---

## 6. Comment Endpoints

### 6.1 Add Comment

**Endpoint:** `POST /api/v1/tickets/{id}/comments`

**Description:** Add a comment to a ticket.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | long | Yes | Ticket ID |

**Request Body:**
```json
{
  "text": "This is a comment",
  "author": "admin"
}
```

**Request DTO:**
```java
public class AddCommentRequest {
    @NotBlank
    @Size(max = 5000)
    private String text;
    
    @NotBlank
    @Size(max = 255)
    private String author;
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "text": "This is a comment",
  "author": "admin",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

**Error Responses:**
- **400 Bad Request:** Validation failed (text blank, author blank, exceeded length)
- **404 Not Found:** Ticket with the given ID does not exist
- **500 Internal Server Error:** Database error or other unexpected error

---

## 7. Enums

### 7.1 Priority Values

| Value | Description |
|-------|-------------|
| LOW | Low priority issue |
| MEDIUM | Medium priority issue |
| HIGH | High priority issue |
| URGENT | Urgent priority issue |

### 7.2 Status Values

| Value | Description |
|-------|-------------|
| OPEN | New ticket, not yet worked on |
| IN_PROGRESS | Ticket is being worked on |
| RESOLVED | Issue has been resolved |
| CANCELLED | Ticket was cancelled |
| CLOSED | Resolved ticket has been closed |

---

## 8. HTTP Status Codes Summary

| Status Code | Usage | Example |
|-------------|-------|---------|
| 200 | Success (GET, PATCH) | Resource retrieved/updated |
| 201 | Created (POST) | Ticket or comment created |
| 400 | Bad Request | Validation failed, invalid input |
| 404 | Not Found | Ticket or comment doesn't exist |
| 409 | Conflict | Invalid state transition |
| 500 | Internal Server Error | Unexpected server error |

---

## 9. Validation Rules

### 9.1 Ticket Creation

| Field | Required | Validation | Error Code |
|-------|----------|------------|------------|
| title | Yes | Not blank, max 255 chars | 400 |
| description | Yes | Not blank, max 10000 chars | 400 |
| priority | Yes | Must be valid enum value | 400 |
| assignee | No | Max 255 chars | 400 |

### 9.2 Ticket Update

| Field | Required | Validation | Error Code |
|-------|----------|------------|------------|
| title | No | Not blank if provided, max 255 chars | 400 |
| description | No | Not blank if provided, max 10000 chars | 400 |
| priority | No | Must be valid enum value if provided | 400 |
| assignee | No | Max 255 chars if provided | 400 |

### 9.3 Comment Addition

| Field | Required | Validation | Error Code |
|-------|----------|------------|------------|
| text | Yes | Not blank, max 5000 chars | 400 |
| author | Yes | Not blank, max 255 chars | 400 |

### 9.4 State Transition

| Field | Required | Validation | Error Code |
|-------|----------|------------|------------|
| targetStatus | Yes | Must be valid enum value | 400 |
| Transition | Yes | Must be valid per state machine | 409 |

---

## 10. Search Behavior

- Case-insensitive substring matching
- Searches across title and description fields
- If search parameter is omitted or empty, no filter is applied
- Maximum search length: 100 characters
- Empty result set is valid when no matches found

---

## 11. Sorting Behavior

- Default sort: createdAt descending, then id descending
- Search and filter results follow same sorting
- Pagination works with sorted results
