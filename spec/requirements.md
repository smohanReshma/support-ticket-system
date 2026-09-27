# Support Ticket Management System - Requirements

## 1. Purpose and Scope

This document defines the requirements for a Support Ticket Management System that allows users to create, manage, and track support tickets. The system provides a backend API with RESTful endpoints for all ticket operations and enforces a strict state machine for ticket status transitions.

**Out of Scope:**
- Authentication and authorization
- Email notifications
- Attachments
- SLA management
- Reporting
- Multi-tenancy
- Mobile applications

## 2. Actors

| Actor | Description |
|-------|-------------|
| End User | Creates, views, updates, and searches tickets through the UI |
| System | Automatically persists data and enforces business rules |

## 3. Functional Requirements

### FR-001: Create Ticket
A user shall be able to create a new support ticket by providing:
- Title (required, non-blank string, max 255 characters)
- Description (required, non-blank string, max 10,000 characters)
- Priority (required: LOW, MEDIUM, HIGH, URGENT)
- Assignee (optional, any non-empty string identifying the assignee, max 255 characters; null is accepted)

**Request:** POST /api/v1/tickets with valid payload

**Response:**
- HTTP 201 Created on success
- Response body contains the created ticket with id, createdAt, and status OPEN

### FR-002: List Tickets
A user shall be able to retrieve a list of all tickets, sorted by createdAt descending, then by id descending if createdAt values are equal.

**Request:** GET /api/v1/tickets

**Response:** HTTP 200 OK with paginated list of tickets

**Query Parameters:**
- `page` (optional, default: 0): Zero-based page number
- `size` (optional, default: 20): Number of tickets per page

**Response Structure:**
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

**Notes:**
- Empty result is valid when no tickets exist

### FR-003: View Ticket Details
A user shall be able to retrieve detailed information for a specific ticket by ID, including all associated comments.

**Request:** GET /api/v1/tickets/{id}

**Response:**
- HTTP 200 OK with complete ticket details on success
- HTTP 404 Not Found if ticket does not exist

### FR-004: Update Ticket
A user shall be able to partially update a ticket using PATCH. Only fields present in the request body are changed. Status cannot be updated through this endpoint.

Valid updatable fields:
- title (optional, non-blank string, max 255 characters if provided)
- description (optional, non-blank string, max 10,000 characters if provided)
- priority (optional, must be LOW, MEDIUM, HIGH, or URGENT if provided)
- assignee (optional, any non-empty string, max 255 characters if provided; null to clear)

**Request:** PATCH /api/v1/tickets/{id} with request body containing fields to update

**Response:**
- HTTP 200 OK with updated ticket on success
- HTTP 400 Bad Request if validation fails
- HTTP 404 Not Found if ticket does not exist

**Notes:**
- Updating these fields does not change the ticket status
- Fields not present in the request body retain their current values

### FR-005: Add Comments
A user shall be able to add comments to a ticket. Comments are append-only and cannot be updated or deleted. Comments are returned in createdAt ascending order.

Comments require:
- Comment text (required, non-blank string, max 5,000 characters)
- Comment author (required, non-blank string, max 255 characters)

**Request:** POST /api/v1/tickets/{id}/comments with text and author

**Response:**
- HTTP 201 Created with the created comment on success
- HTTP 404 Not Found if ticket does not exist

### FR-006: Search Tickets by Keyword
A user shall be able to search tickets by keyword across title and description fields.

**Request:** GET /api/v1/tickets?search={keyword}

**Search behavior:**
- Case-insensitive substring matching across title and description
- If search parameter is omitted, no search filter is applied
- If search parameter is empty string (e.g., ?search=), no search filter is applied
- If search parameter is supplied, it must be no more than 100 characters

**Response:**
- HTTP 200 OK with matching tickets on success
- HTTP 400 Bad Request if search exceeds 100 characters

**Notes:**
- Empty result set is valid when no tickets match

### FR-007: Filter Tickets by Status
A user shall be able to filter tickets by status using the status query parameter.

**Request:** GET /api/v1/tickets?status={status}

**Valid status values:** OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED (case-sensitive, exact match required)

**Combined with search:** GET /api/v1/tickets?search={keyword}&status={status} returns tickets matching both criteria

**Response:**
- HTTP 200 OK with filtered tickets on success
- HTTP 400 Bad Request if status value is invalid
- HTTP 200 OK with empty list when no tickets match the filter

### FR-008: State Transition
A user shall be able to transition a ticket to a new status through a dedicated endpoint.

**Request:** POST /api/v1/tickets/{id}/transition

**Request body:**
```json
{
  "targetStatus": "IN_PROGRESS"
}
```

**targetStatus must be:**
- One of: OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED
- Provided in the request body as a field named targetStatus

**Response:**
- HTTP 200 OK with updated ticket on successful valid transition
- HTTP 400 Bad Request if targetStatus is missing, malformed, or not a valid status value
- HTTP 404 Not Found if ticket does not exist
- HTTP 409 Conflict if the transition is not allowed by the state machine

**Notes:**
- State transitions are enforced by the backend
- State changes are persisted to the database

### FR-009: UI Error Display
The UI shall present validation errors from the backend to the user in a clear and accessible manner.
The UI shall present state transition errors (409 Conflict) to the user with information about valid next states.
The UI shall present resource not found errors (404) to the user when applicable.
Error messages shall be displayed prominently and remain visible until the user dismisses them or the underlying issue is resolved.
Error messages shall be presented in a way that does not block user interaction with other parts of the application.

## 4. Non-Functional Requirements

### NFR-001: Performance
The system shall respond to requests within 2 seconds under normal load.

### NFR-002: Maintainability
The system shall follow a layered architecture with clear separation of concerns.

### NFR-003: Testability
All functionality shall be testable via automated tests.

### NFR-004: Code Quality
All code shall follow the project's coding standards and architecture rules.

## 5. Ticket Fields and Business Rules

| Field | Type | Required | Constraints | Default |
|-------|------|----------|-------------|---------|
| id | Long | Yes (auto-generated) | Positive integer | Auto-increment |
| title | String | Yes | Non-blank, max 255 chars | None |
| description | String | Yes | Non-blank, max 10,000 chars | None |
| priority | Enum | Yes | LOW, MEDIUM, HIGH, URGENT | None |
| status | Enum | Yes (auto-set) | OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED | OPEN (initial) |
| assignee | String | No | Any non-empty string, max 255 chars, or null | null |
| createdAt | Instant | Yes (auto-generated) | Valid timestamp | Current time |
| updatedAt | Instant | Yes (auto-generated) | Valid timestamp | Current time |

**Timestamp behavior:**
- createdAt is set when the ticket is created and never changes
- updatedAt is set when the ticket is created
- updatedAt changes whenever a ticket field is successfully modified (including status transitions)

**Business Rules:**
- All tickets start with status OPEN
- Priority values are: LOW, MEDIUM, HIGH, URGENT
- Status values are: OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED
- Assignee is an arbitrary identifier string (no user account required)

## 6. Comment Requirements

| Field | Type | Required | Constraints | Default |
|-------|------|----------|-------------|---------|
| id | Long | Yes (auto-generated) | Positive integer | Auto-increment |
| ticketId | Long | Yes | Positive integer | None |
| text | String | Yes | Non-blank, max 5,000 chars | None |
| author | String | Yes | Non-blank, max 255 chars | None |
| createdAt | Instant | Yes (auto-generated) | Valid timestamp | Current time |

**CR-001:** Comments are associated with a ticket via ticketId.

**CR-002:** Comments are retrieved as part of ticket details.

**CR-003:** Comments are append-only and cannot be updated or deleted. Comments are returned in createdAt ascending order.

## 7. Search Requirements

### SR-001: Keyword Search
Search shall be performed across title and description fields using case-insensitive substring matching.

**Testable:** `GET /api/v1/tickets?search=login` finds tickets containing "login", "Login", "LOGIN", etc.

### SR-002: Empty/Omitted Search Parameter
If search parameter is omitted or empty, no search filter is applied.

**Testable:** `GET /api/v1/tickets` and `GET /api/v1/tickets?search=` return all tickets

### SR-003: Search Length Limit
If search parameter is supplied, it must be no more than 100 characters.

**Testable:** `GET /api/v1/tickets?search={101+ characters}` returns HTTP 400

### SR-004: Empty Results
Search shall return empty results when no matches are found.

**Testable:** `GET /api/v1/tickets?search=nonexistent` returns empty list

## 8. Filtering Requirements

### FR-007-FC: Filter by Status
Tickets shall be filterable by status using the `status` query parameter.

**Testable:** `GET /api/v1/tickets?status=OPEN` returns only tickets with status OPEN

### FR-007-VC: Valid Status Values
Only the following status values are valid for filtering: OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED (case-sensitive, exact match)

**Testable:** `GET /api/v1/tickets?status=INVALID` returns HTTP 400 Bad Request

### FR-007-SC: Combined Search and Status
Search and status filtering may be combined in a single request.

**Testable:** `GET /api/v1/tickets?search=login&status=OPEN` returns OPEN tickets matching "login"

### FR-007-EC: Empty Results
An empty result set is valid when no tickets match the filter criteria.

**Testable:** `GET /api/v1/tickets?status=RESOLVED` returns an empty list when no RESOLVED tickets exist

## 9. Validation Requirements

### VR-001: Title Validation
- Required for creation: Yes
- Required for PATCH: No (optional)
- Non-blank if provided: Yes
- Max length: 255 characters

**Testable:** Empty or blank title on create returns 400; blank title in PATCH returns 400

### VR-002: Description Validation
- Required for creation: Yes
- Required for PATCH: No (optional)
- Non-blank if provided: Yes
- Max length: 10,000 characters

**Testable:** Empty or blank description on create returns 400; blank description in PATCH returns 400

### VR-003: Priority Validation
- Required for creation: Yes
- Required for PATCH: No (optional)
- Valid values if provided: LOW, MEDIUM, HIGH, URGENT

**Testable:** Invalid priority on create returns 400; invalid priority in PATCH returns 400

### VR-004: Assignee Validation
- Optional for creation: Yes (null is accepted)
- Optional for PATCH: Yes (null to clear, non-empty string if provided)
- If provided: Non-empty string, max 255 characters
- If null: Accepted (to clear assignee)

**Testable:** Empty string assignee in PATCH returns 400; null assignee is accepted

### VR-005: Comment Text Validation
- Required: Yes
- Non-blank: Yes
- Max length: 5,000 characters

**Testable:** Empty or blank comment text returns 400 with error message

### VR-006: Comment Author Validation
- Required: Yes
- Non-blank: Yes
- Max length: 255 characters

**Testable:** Empty or blank comment author returns 400 with error message

### VR-007: Ticket ID Validation
- Required for ticket-specific operations: Yes
- Must reference existing ticket
- Valid status for state transitions: Yes

**Testable:** Non-existent ticket ID returns 404; invalid state value returns 400

### VR-008: Search Length Validation
- If supplied: Max 100 characters

**Testable:** Search exceeding 100 characters returns 400

## 10. State-Machine Requirements

### SM-001: Initial State
All new tickets start with status OPEN.

**Testable:** Created ticket has status OPEN

### SM-002: Valid Transitions from OPEN

| From | To | Description |
|------|----|-------------|
| OPEN | IN_PROGRESS | Open ticket can be started |
| OPEN | CANCELLED | Open ticket can be cancelled |

**SM-002-TC1:** Transition OPEN -> IN_PROGRESS is valid.

**SM-002-TC2:** Transition OPEN -> CANCELLED is valid.

### SM-003: Valid Transitions from IN_PROGRESS

| From | To | Description |
|------|----|-------------|
| IN_PROGRESS | RESOLVED | In-progress ticket can be resolved |
| IN_PROGRESS | CANCELLED | In-progress ticket can be cancelled |

**SM-003-TC1:** Transition IN_PROGRESS -> RESOLVED is valid.

**SM-003-TC2:** Transition IN_PROGRESS -> CANCELLED is valid.

### SM-004: Valid Transitions from RESOLVED

| From | To | Description |
|------|----|-------------|
| RESOLVED | CLOSED | Resolved ticket can be closed |

**SM-004-TC1:** Transition RESOLVED -> CLOSED is valid.

### SM-005: Valid Transitions from CANCELLED

| From | To | Description |
|------|----|-------------|
| None | None | No valid transitions from cancelled |

**SM-005-TC1:** Any transition from CANCELLED is invalid.

### SM-006: Valid Transitions from CLOSED

| From | To | Description |
|------|----|-------------|
| None | None | No valid transitions from closed |

**SM-006-TC1:** Any transition from CLOSED is invalid.

### SM-007: Invalid Transitions (Explicit Table)

Valid transitions are:
- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

All other transitions are invalid:

| From | To | Response | Reason |
|------|----|----------|--------|
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

**SM-007-TC1:** Valid status value + disallowed transition = HTTP 409 Conflict.

**SM-007-TC2:** Invalid/malformed targetStatus value = HTTP 400 Bad Request.

**SM-007-TC3:** All invalid transitions return HTTP 409 Conflict.

### SM-008: Error Response for Invalid Transitions
When an invalid transition is attempted, the response shall include:
- HTTP 409 Conflict status (for valid status + disallowed transition)
- HTTP 400 Bad Request (for invalid/malformed status value)
- Error message indicating the current state and requested state
- List of valid transitions for the current state

**Testable:** Request to transition OPEN -> RESOLVED returns 409 with message and valid transitions [IN_PROGRESS, CANCELLED]

### SM-009: State Change Persistence
State changes must be persisted to the database.

**Testable:** After state transition, retrieving the ticket shows the new status

## 11. Error-Handling Requirements

### ER-001: Validation Errors
Invalid input data shall return HTTP 400 Bad Request.

**ER-001-TC1:** Response includes validation error details.

**ER-001-TC2:** Response includes field name and error message for each validation failure.

### ER-002: Resource Not Found
Non-existent tickets shall return HTTP 404 Not Found.

**ER-002-TC1:** Response includes ticket ID in error message.

### ER-003: Invalid State Transition
Invalid state transitions shall return HTTP 409 Conflict.

**ER-003-TC1:** Response includes current state and requested state.

**ER-003-TC2:** Response includes list of valid transitions for current state.

### ER-004: Generic Errors
Unexpected errors shall return HTTP 500 Internal Server Error.

**ER-004-TC1:** Response includes generic error message.

**ER-004-TC2:** Server logs include detailed error information.

### ER-005: Error Response Format
All error responses shall follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tickets"
}
```

**Validation Error Response Format:**
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

**Invalid State Transition Error Response Format:**
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

## 12. Persistence Requirements

### PR-001: Database Persistence
All tickets and comments shall be persisted in a relational database.

**Testable:** Data survives application restart.

### PR-002: Transactional Operations
State changes and updates shall be transactional.

**Testable:** Failed updates do not persist partial changes.

### PR-003: Timestamp Tracking
Tickets shall track creation and last update timestamps.

**Testable:** createdAt and updatedAt are set automatically.

### PR-004: Data Integrity
Database constraints shall enforce data integrity.

**Testable:** Duplicate ticket IDs cannot be created.

## 13. Security/Secrets Requirements

### SEC-001: No Hardcoded Secrets
No secrets (API keys, passwords, tokens) shall be hardcoded in source code.

**Testable:** All configuration values are externalized.

### SEC-002: Externalized Configuration
Configuration shall be loaded from environment variables or external config.

**Testable:** Database credentials not in application.properties.

### SEC-003: No Secrets in Logs
No secrets shall appear in application logs.

**Testable:** Log files do not contain passwords or API keys.

### SEC-004: SQL Injection Prevention
The system shall use parameterized queries to prevent SQL injection attacks. SQL injection-like input in search queries shall be treated as ordinary search text and must not alter the query execution or database state.

**Testable:** Search with SQL injection payload (e.g., `'; DROP TABLE tickets; --`) returns results based on literal string matching and does not cause database errors or data loss.

## 14. Acceptance Criteria

### AC-001: Ticket Creation Success
**Given** A user submits a valid ticket creation request
**When** The request is processed
**Then** A ticket is created with status OPEN, valid ID, and timestamps set
**And** The response returns HTTP 201 Created with the created ticket

**Test Mapping:** CreateTicketRequest with valid data -> 201 response with TicketResponse

### AC-002: Ticket Creation Validation Failure
**Given** A user submits an invalid ticket creation request (e.g., blank title)
**When** The backend validates the input
**Then** HTTP 400 Bad Request with validation errors is returned

**Test Mapping:** CreateTicketRequest with blank title -> 400 response with violations

### AC-003: Ticket List
**Given** A user requests the ticket list
**When** The request is processed
**Then** A paginated list of tickets is returned with content, page, size, totalElements, and totalPages

**Test Mapping:** GET /api/v1/tickets?page=0&size=20 -> TicketListResponse with pagination metadata

### AC-004: Ticket Detail
**Given** A user requests a specific ticket
**When** The ticket exists
**Then** Ticket details including comments (sorted by createdAt ascending) are returned

**Test Mapping:** GET /api/v1/tickets/{id} -> TicketResponse with comments

### AC-005: Ticket PATCH Partial Update
**Given** A user updates ticket fields using PATCH
**When** The update is valid
**Then** Only provided fields are updated, status remains unchanged, and updatedAt is updated

**Test Mapping:** PATCH /api/v1/tickets/{id} with partial data -> Updated TicketResponse

### AC-006: Ticket PATCH Validation Failure
**Given** A user submits invalid data in PATCH request
**When** The backend validates the input
**Then** HTTP 400 Bad Request with validation errors is returned

**Test Mapping:** PATCH /api/v1/tickets/{id} with blank title -> 400 response with violations

### AC-007: Ticket PATCH Non-Existent
**Given** A user updates a non-existent ticket using PATCH
**When** The ticket ID does not exist
**Then** HTTP 404 Not Found is returned

**Test Mapping:** PATCH /api/v1/tickets/999999 -> 404 response

### AC-008: Comment Addition
**Given** A user adds a comment to a ticket
**When** The comment is valid
**Then** The comment is added to the ticket

**Test Mapping:** POST /api/v1/tickets/{id}/comments -> CommentResponse

### AC-009: Comment Addition Non-Existent
**Given** A user adds a comment to a non-existent ticket
**When** The ticket ID does not exist
**Then** HTTP 404 Not Found is returned

**Test Mapping:** POST /api/v1/tickets/999999/comments -> 404 response

### AC-009a: UI Error Display - Validation Errors
**Given** A user submits invalid data in a request
**When** The backend returns validation errors
**Then** The UI presents the validation errors to the user clearly

**Test Mapping:** Validation error response -> UI displays error message

### AC-009b: UI Error Display - Transition Errors
**Given** A user attempts an invalid state transition
**When** The backend returns a 409 Conflict with valid transitions
**Then** The UI presents the error and available transitions to the user

**Test Mapping:** 409 response with validTransitions -> UI displays error with options

### AC-009c: UI Error Display - Resource Not Found
**Given** A user requests a non-existent resource
**When** The backend returns 404 Not Found
**Then** The UI presents an appropriate message to the user

**Test Mapping:** 404 response -> UI displays "not found" message

### AC-010: Comment Chronological Order
**Given** A ticket has multiple comments
**When** Comments are retrieved as part of ticket details
**Then** Comments are returned in createdAt ascending order

**Test Mapping:** GET /api/v1/tickets/{id} -> Comments sorted by createdAt ascending

### AC-011: Search by Keyword
**Given** A user searches tickets by keyword
**When** The keyword matches title or description
**Then** Matching tickets are returned

**Test Mapping:** GET /api/v1/tickets?search=login -> TicketListResponse with matching tickets

### AC-012: Filter by Status
**Given** A user filters tickets by status
**When** The status filter is valid
**Then** Only tickets with that status are returned

**Test Mapping:** GET /api/v1/tickets?status=OPEN -> TicketListResponse with only OPEN tickets

### AC-013: Invalid Status Filter
**Given** A user filters tickets with an invalid status value
**When** The status value is not OPEN, IN_PROGRESS, RESOLVED, CANCELLED, or CLOSED
**Then** HTTP 400 Bad Request is returned

**Test Mapping:** GET /api/v1/tickets?status=INVALID -> 400 response

### AC-014: Combined Search and Status Filter
**Given** A user searches and filters tickets simultaneously
**When** Both search and status parameters are provided
**Then** Tickets matching both criteria are returned

**Test Mapping:** GET /api/v1/tickets?search=login&status=OPEN -> OPEN tickets matching "login"

### AC-015: State Transition Valid
**Given** A user attempts a valid state transition
**When** The transition follows the state machine
**Then** The ticket status is updated and updatedAt is changed

**Test Mapping:** POST /api/v1/tickets/{id}/transition with valid target -> Updated TicketResponse with new status

### AC-016: State Transition Invalid Status Value
**Given** A user attempts a state transition with an invalid status value
**When** The targetStatus is missing, malformed, or not a valid status value
**Then** HTTP 400 Bad Request is returned

**Test Mapping:** POST /api/v1/tickets/{id}/transition with invalid target -> 400 response

### AC-017: State Transition Disallowed
**Given** A user attempts an invalid state transition
**When** The transition violates the state machine (valid status but disallowed transition)
**Then** HTTP 409 Conflict is returned with current state, requested state, and valid transitions

**Test Mapping:** POST /api/v1/tickets/{id}/transition with valid but disallowed target -> 409 response

### AC-018: Database Persistence
**Given** A ticket is created
**When** The application restarts
**Then** The ticket data persists

**Test Mapping:** Create ticket -> Restart app -> GET ticket exists

### AC-019: SQL Injection Prevention
**Given** A user searches with SQL injection-like input
**When** The search query uses parameterized queries
**Then** The SQL injection payload is treated as ordinary search text and does not cause database errors or data loss

**Test Mapping:** GET /api/v1/tickets?search='; DROP TABLE tickets; -- -> Empty or sanitized results, no database error