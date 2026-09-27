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
- Frontend implementation

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
- Assignee (optional, any non-empty string identifying the assignee, max 255 characters)

**Testable:** A POST request to `/api/v1/tickets` with valid payload creates a ticket with status OPEN.

### FR-002: List Tickets
A user shall be able to retrieve a list of all tickets, sorted by creation date descending.

**Testable:** A GET request to `/api/v1/tickets` returns a list of all tickets.

### FR-003: View Ticket Details
A user shall be able to retrieve detailed information for a specific ticket by ID.

**Testable:** A GET request to `/api/v1/tickets/{id}` returns complete ticket details including comments.

### FR-004: Update Ticket
A user shall be able to update the title, description, priority, and assignee of a ticket.

**Testable:** A PATCH/PUT request to `/api/v1/tickets/{id}` updates the specified fields.

### FR-005: Add Comments
A user shall be able to add comments to a ticket. Comments require:
- Comment text (required, non-blank string, max 5,000 characters)
- Comment author (required, any non-empty string identifying the author, max 255 characters)

**Testable:** A POST request to `/api/v1/tickets/{id}/comments` adds a comment to the ticket.

### FR-006: Search Tickets by Keyword
A user shall be able to search tickets by keyword across title and description fields.

**Testable:** A GET request to `/api/v1/tickets?search=keyword` returns tickets where title or description contains the keyword (case-insensitive partial match).

### FR-007: Filter Tickets by Status
A user shall be able to filter tickets by status.

**Testable:** A GET request to `/api/v1/tickets?status=OPEN` returns only tickets with the specified status.

### FR-008: State Transition Enforcement
The backend shall enforce the ticket state machine and reject invalid transitions.

**Testable:** A state transition request with an invalid transition returns HTTP 409 Conflict with details about valid transitions.

### FR-009: Input Validation
The backend shall validate all input and return meaningful error messages.

**Testable:** A request with invalid data returns HTTP 400 Bad Request with validation error details.

### FR-010: Database Persistence
All tickets and comments shall be persisted in a database.

**Testable:** Data persists across application restarts and can be queried from the database.

## 4. Non-Functional Requirements

### NFR-001: Performance
The system shall respond to requests within 2 seconds under normal load.

### NFR-002: Reliability
The system shall not crash on valid input and shall process requests successfully when data is valid.

### NFR-003: Maintainability
The system shall follow a layered architecture with clear separation of concerns.

### NFR-004: Testability
All functionality shall be testable via automated tests.

### NFR-005: Code Quality
All code shall follow the project's coding standards and architecture rules.

## 5. Ticket Fields and Business Rules

| Field | Type | Required | Constraints | Default |
|-------|------|----------|-------------|---------|
| id | Long | Yes (auto-generated) | Positive integer | Auto-increment |
| title | String | Yes | Non-blank, max 255 chars | None |
| description | String | Yes | Non-blank, max 10,000 chars | None |
| priority | Enum | Yes | LOW, MEDIUM, HIGH, URGENT | None |
| status | Enum | Yes (auto-set) | OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED | OPEN (initial) |
| assignee | String | No | Any non-empty string, max 255 chars | null |
| createdAt | Instant | Yes (auto-generated) | Valid timestamp | Current time |
| updatedAt | Instant | Yes (auto-generated) | Valid timestamp | Current time |

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

**CR:001:** Comments are associated with a ticket via ticketId.

**CR:002:** Comments are retrieved as part of ticket details.

**CR:003:** Comments cannot be updated or deleted (append-only).

**Comment Author Note:** Author is a free-form string identifier. No user account or authentication is required.

## 7. Search Requirements

### SR-001: Keyword Search
Search shall be performed across title and description fields.

**Testable:** `GET /api/v1/tickets?search=login` finds tickets containing "login" in title or description.

### SR-002: Case-Insensitive
Search shall be case-insensitive.

**Testable:** `GET /api/v1/tickets?search=LOGIN` finds tickets with "login", "Login", "LOGIN".

### SR-003: Partial Match
Search shall support partial matches within words.

**Testable:** `GET /api/v1/tickets?search=log` finds tickets containing "login", "logout", "dialog".

### SR-004: Empty Results
Search shall return empty results when no matches are found.

**Testable:** `GET /api/v1/tickets?search=nonexistent` returns empty list.

## 8. Filtering Requirements

### FR-007-FC: Filter by Status
Tickets shall be filterable by status using the `status` query parameter.

**Testable:** `GET /api/v1/tickets?status=OPEN` returns only tickets with status OPEN.

### FR-007-VC: Valid Status Values
Only the following status values are valid for filtering: OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED.

**Testable:** `GET /api/v1/tickets?status=INVALID` returns HTTP 400 Bad Request.

### FR-007-SC: Combined Search and Status
Search and status filtering may be combined in a single request.

**Testable:** `GET /api/v1/tickets?search=login&status=OPEN` returns OPEN tickets matching "login".

### FR-007-EC: Empty Results
An empty result set is valid when no tickets match the filter criteria.

**Testable:** `GET /api/v1/tickets?status=RESOLVED` returns an empty list when no RESOLVED tickets exist.

## 9. Validation Requirements

### VR-001: Title Validation
- Required: Yes
- Non-blank: Yes
- Max length: 255 characters

**Testable:** Empty or blank title returns 400 with error message.

### VR-002: Description Validation
- Required: Yes
- Non-blank: Yes
- Max length: 10,000 characters

**Testable:** Empty or blank description returns 400 with error message.

### VR-003: Priority Validation
- Required: Yes
- Valid values: LOW, MEDIUM, HIGH, URGENT

**Testable:** Invalid priority value returns 400 with error message.

### VR-004: Assignee Validation
- Optional: Yes
- If provided: Must be non-empty string, max 255 characters
- If null: Accepted

**Testable:** Empty string assignee returns 400 with error message.

### VR-005: Comment Text Validation
- Required: Yes
- Non-blank: Yes
- Max length: 5,000 characters

**Testable:** Empty or blank comment text returns 400 with error message.

### VR-006: Comment Author Validation
- Required: Yes
- Non-blank: Yes
- Max length: 255 characters

**Testable:** Empty or blank comment author returns 400 with error message.

### VR-007: Ticket ID Validation
- Required for ticket-specific operations
- Must be positive integer
- Must reference existing ticket

**Testable:** Non-existent ticket ID returns 404 with error message.

## 10. State-Machine Requirements

### SM-001: Initial State
All new tickets start with status OPEN.

**Testable:** Created ticket has status OPEN.

### SM-002: Valid Transitions from OPEN

| From | To | Condition |
|------|----|-----------|
| OPEN | IN_PROGRESS | Any ticket |
| OPEN | CANCELLED | Any ticket |

**SM-002-TC1:** Transition OPEN -> IN_PROGRESS is valid.
**SM-002-TC2:** Transition OPEN -> CANCELLED is valid.

### SM-003: Valid Transitions from IN_PROGRESS

| From | To | Condition |
|------|----|-----------|
| IN_PROGRESS | RESOLVED | Any ticket |
| IN_PROGRESS | CANCELLED | Any ticket |

**SM-003-TC1:** Transition IN_PROGRESS -> RESOLVED is valid.
**SM-003-TC2:** Transition IN_PROGRESS -> CANCELLED is valid.

### SM-004: Valid Transitions from RESOLVED

| From | To | Condition |
|------|----|-----------|
| RESOLVED | CLOSED | Any ticket |

**SM-004-TC1:** Transition RESOLVED -> CLOSED is valid.

### SM-005: Valid Transitions from CANCELLED

| From | To | Condition |
|------|----|-----------|
| CANCELLED | None | No valid transitions |

**SM-005-TC1:** Any transition from CANCELLED is invalid.

### SM-006: Valid Transitions from CLOSED

| From | To | Condition |
|------|----|-----------|
| CLOSED | None | No valid transitions |

**SM-006-TC1:** Any transition from CLOSED is invalid.

### SM-007: Invalid Transitions Must Be Rejected
All invalid transitions must be rejected with HTTP 409 Conflict.

**SM-007-TC1:** CLOSED -> OPEN rejected.
**SM-007-TC2:** RESOLVED -> OPEN rejected.
**SM-007-TC3:** CANCELLED -> OPEN rejected.

### SM-008: Error Response for Invalid Transitions
When an invalid transition is attempted, the response shall include:
- HTTP 409 Conflict status
- Error message indicating the invalid transition
- List of valid transitions for the current state

**Testable:** Request to transition OPEN -> RESOLVED returns 409 with message and valid transitions.

### SM-009: State Change Persistence
State changes must be persisted to the database.

**Testable:** After state transition, retrieving the ticket shows the new status.

## 11. Error-Handling Requirements

### ER-001: Validation Errors
Invalid input data shall return HTTP 400 Bad Request.

**ER-001-TC1:** Response includes validation error details.

**ER-001-TC2:** Response includes field name and error message.

### ER-002: Resource Not Found
Non-existent tickets shall return HTTP 404 Not Found.

**ER-002-TC1:** Response includes ticket ID in error message.

### ER-003: Invalid State Transition
Invalid state transitions shall return HTTP 409 Conflict.

**ER-003-TC1:** Response includes current state and requested state.

**ER-003-TC2:** Response includes list of valid transitions.

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

## 13. UI Requirements

### UI-001: Error Display
The UI shall display meaningful error messages to users.

**Testable:** Validation errors are displayed near the relevant input field.

**UI-001-TC1:** State transition errors show valid transitions.

### UI-002: Form Validation
The UI shall validate input before sending to the backend.

**Testable:** Missing required fields show validation messages.

### UI-003: Status Display
The UI shall display the current ticket status.

**Testable:** Ticket details show current status.

### UI-004: Status Change Feedback
The UI shall provide feedback when status changes.

**Testable:** Successful state transition shows updated status.

### UI-005: Loading States
The UI shall show loading states during async operations.

**Testable:** Operations show loading indicator.

## 14. Security/Secrets Requirements

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

## 15. Acceptance Criteria

### AC-001: Ticket Creation
**Given** A user submits a valid ticket creation request
**When** The request is processed
**Then** A ticket is created with status OPEN, valid ID, and timestamps set

**Test Mapping:** CreateTicketRequest with valid data -> TicketResponse with status OPEN

### AC-002: Ticket List
**Given** A user requests the ticket list
**When** The request is processed
**Then** A list of all tickets is returned, sorted by createdAt descending

**Test Mapping:** GET /api/v1/tickets -> TicketListResponse with all tickets

### AC-003: Ticket Detail
**Given** A user requests a specific ticket
**When** The ticket exists
**Then** Ticket details including comments are returned

**Test Mapping:** GET /api/v1/tickets/{id} -> TicketResponse with comments

### AC-004: Ticket Update
**Given** A user updates ticket fields
**When** The update is valid
**Then** The ticket is updated with new values

**Test Mapping:** PATCH /api/v1/tickets/{id} with new data -> Updated TicketResponse

### AC-005: Comment Addition
**Given** A user adds a comment to a ticket
**When** The comment is valid
**Then** The comment is added to the ticket

**Test Mapping:** POST /api/v1/tickets/{id}/comments -> CommentResponse

### AC-006: Search by Keyword
**Given** A user searches tickets by keyword
**When** The keyword matches title or description
**Then** Matching tickets are returned

**Test Mapping:** GET /api/v1/tickets?search=login -> TicketListResponse with matching tickets

### AC-007: Filter by Status
**Given** A user filters tickets by status
**When** The status filter is valid
**Then** Only tickets with that status are returned

**Test Mapping:** GET /api/v1/tickets?status=OPEN -> TicketListResponse with only OPEN tickets

### AC-008: State Transition Valid
**Given** A user attempts a valid state transition
**When** The transition follows the state machine
**Then** The ticket status is updated

**Test Mapping:** POST /api/v1/tickets/{id}/transition with valid target -> Updated TicketResponse

### AC-009: State Transition Invalid
**Given** A user attempts an invalid state transition
**When** The transition violates the state machine
**Then** HTTP 409 Conflict is returned with valid transitions listed

**Test Mapping:** POST /api/v1/tickets/{id}/transition with invalid target -> 409 response

### AC-010: Input Validation Error
**Given** A user submits invalid data
**When** The backend validates the input
**Then** HTTP 400 Bad Request with validation errors is returned

**Test Mapping:** POST /api/v1/tickets with blank title -> 400 response with violations

### AC-011: Database Persistence
**Given** A ticket is created
**When** The application restarts
**Then** The ticket data persists

**Test Mapping:** Create ticket -> Restart app -> GET ticket exists