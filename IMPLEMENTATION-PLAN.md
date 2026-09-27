# Support Ticket Management System - Implementation Plan

This plan covers the full assignment implementation based on the specification artifacts.

---

## 1. Project/Backend Foundation

### Task 1.1: Project Initialization
- **Task ID:** 1.1
- **Description:** Set up Gradle project with Spring Boot 4.1.1 and Java 21
- **Files to Create/Modify:**
  - `build.gradle` - Configure dependencies (Spring Boot, JPA, Validation, PostgreSQL, H2)
  - `settings.gradle` - Project configuration
  - `gradle/wrapper/gradle-wrapper.properties` - Gradle version
- **Dependencies/Prerequisites:** Java 21 SDK installed
- **Relevant IDs:** architecture.md (Tech Stack), requirements.md (NFR-002, NFR-004)
- **Completion Criteria:**
  - `./gradlew build` completes successfully
  - Project runs with `./gradlew bootRun`
  - H2 in-memory database works for development

### Task 1.2: Database Configuration
- **Task ID:** 1.2
- **Description:** Configure PostgreSQL/H2 database connections
- **Files to Create/Modify:**
  - `src/main/resources/application.properties` - Database and JPA configuration
  - `src/main/resources/application-local.properties` - Local dev settings
- **Dependencies/Prerequisites:** Task 1.1 completed
- **Relevant IDs:** architecture.md (Database Configuration), data-model.md (Database Migrations)
- **Completion Criteria:**
  - H2 database works in development profile
  - PostgreSQL configuration externalized via environment variables
  - No hardcoded secrets in configuration
## TODO: pick all tasks only new tokens are available
### Task 1.3: Project Structure Setup
- **Task ID:** 1.3
- **Description:** Create package structure per architecture.md
- **Files to Create/Modify:**
  - Create directories: `config/`, `controller/`, `service/`, `repository/`, `model/`, `dto/`, `exception/`, `mapper/`
  - `src/main/java/org/c2/supportticketsystem/` - Base package structure
- **Dependencies/Prerequisites:** Tasks 1.1, 1.2 completed
- **Relevant IDs:** architecture.md (Package Structure)
- **Completion Criteria:**
  - All required package directories exist
  - Package naming follows `org.c2.supportticketsystem` convention

---

## 2. Data Model and Persistence

### Task 2.1: Enumerations
- **Task ID:** 2.1
- **Description:** Create Status and Priority enum types
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/model/enums/Status.java`
  - `src/main/java/org/c2/supportticketsystem/model/enums/Priority.java`
- **Dependencies/Prerequisites:** Task 1.3 completed
- **Relevant IDs:** data-model.md (Enumerations), requirements.md (Ticket Fields)
- **Completion Criteria:**
  - Status enum: OPEN, IN_PROGRESS, RESOLVED, CANCELLED, CLOSED
  - Priority enum: LOW, MEDIUM, HIGH, URGENT
  - Enums use `@Enumerated(EnumType.STRING)` mapping

### Task 2.2: Ticket Entity
- **Task ID:** 2.2
- **Description:** Create Ticket JPA entity
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/model/Ticket.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.1 completed
- **Relevant IDs:** data-model.md (Ticket Entity), requirements.md (Ticket Fields)
- **Completion Criteria:**
  - All fields: id, title, description, priority, status, assignee, createdAt, updatedAt
  - Constraints: @NotBlank, @Size(max), @Enumerated
  - JPA annotations: @Entity, @Table, @Column, @Enumerated
  - Timestamps set automatically (createdAt, updatedAt)

### Task 2.3: Comment Entity
- **Task ID:** 2.3
- **Description:** Create Comment JPA entity
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/model/Comment.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.1 completed
- **Relevant IDs:** data-model.md (Comment Entity), requirements.md (Comment Requirements)
- **Completion Criteria:**
  - All fields: id, ticketId, text, author, createdAt
  - Constraints: @NotBlank, @Size(max)
  - @Column(name = "ticket_id", nullable = false)
  - @Column(name = "created_at", nullable = false)

### Task 2.4: Repositories
- **Task ID:** 2.4
- **Description:** Create JPA repositories
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/repository/TicketRepository.java`
  - `src/main/java/org/c2/supportticketsystem/repository/CommentRepository.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.2, 2.3 completed
- **Relevant IDs:** data-model.md (Repository Methods), requirements.md (FR-002, FR-006, FR-007)
- **Completion Criteria:**
  - TicketRepository: findAll(), findById(), save(), findByStatus(), search(), findByStatusAndSearch(), pagination
  - CommentRepository: findAll(), findById(), save(), findByTicketId()
  - Search uses parameterized queries (SQL injection prevention - AC-019)

### Task 2.5: Mappers
- **Task ID:** 2.5
- **Description:** Create DTO-Entity mapping classes
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/mapper/TicketMapper.java`
  - `src/main/java/org/c2/supportticketsystem/mapper/CommentMapper.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.2, 2.3 completed
- **Relevant IDs:** architecture.md (DTO Pattern), api-contract.md (DTO Usage)
- **Completion Criteria:**
  - Entity to Response DTO conversion
  - Request DTO to Entity conversion
  - Proper timestamp handling

---

## 3. Backend Domain/Business Logic

### Task 3.1: Ticket Creation Service
- **Task ID:** 3.1
- **Description:** Implement ticket creation business logic
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.2, 2.5 completed
- **Relevant IDs:** requirements.md (FR-001, AC-001, AC-002)
- **Completion Criteria:**
  - POST /api/v1/tickets endpoint
  - Validation at service layer
  - Returns 201 Created with ticket
  - Creates ticket with status OPEN

### Task 3.2: Ticket Listing with Pagination
- **Task ID:** 3.2
- **Description:** Implement paginated ticket listing
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java` (add method)
- **Dependencies/Prerequisites:** Task 3.1 completed
- **Relevant IDs:** requirements.md (FR-002, AC-003), api-contract.md (Pagination)
- **Completion Criteria:**
  - GET /api/v1/tickets endpoint
  - Returns Page<TicketResponse> with content, page, size, totalElements, totalPages
  - Sorted by createdAt DESC, then id DESC
  - Supports page and size query parameters (default: page=0, size=20)

### Task 3.3: Ticket Details Retrieval
- **Task ID:** 3.3
- **Description:** Implement ticket detail retrieval with comments
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java` (add method)
- **Dependencies/Prerequisites:** Task 3.2 completed
- **Relevant IDs:** requirements.md (FR-003, AC-004)
- **Completion Criteria:**
  - GET /api/v1/tickets/{id} endpoint
  - Returns TicketResponse with comments
  - Comments sorted by createdAt ASC
  - Returns 404 if ticket not found

### Task 3.4: Ticket Partial Update
- **Task ID:** 3.4
- **Description:** Implement PATCH partial update
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java` (add method)
- **Dependencies/Prerequisites:** Task 3.3 completed
- **Relevant IDs:** requirements.md (FR-004, AC-005, AC-006, AC-007)
- **Completion Criteria:**
  - PATCH /api/v1/tickets/{id} endpoint
  - Only updates provided fields
  - Status cannot be updated via PATCH
  - Updates updatedAt timestamp
  - Returns 404 if ticket not found

### Task 3.5: Comment Addition
- **Task ID:** 3.5
- **Description:** Implement comment addition
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/CommentService.java`
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java` (add method)
- **Dependencies/Prerequisites:** Tasks 1.3, 2.3, 2.5 completed
- **Relevant IDs:** requirements.md (FR-005, AC-008, AC-009)
- **Completion Criteria:**
  - POST /api/v1/tickets/{id}/comments endpoint
  - Creates comment with ticket association
  - Returns 201 Created with comment
  - Returns 404 if ticket not found

### Task 3.6: Search and Filter
- **Task ID:** 3.6
- **Description:** Implement search and status filtering
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java` (add methods)
- **Dependencies/Prerequisites:** Task 3.2 completed
- **Relevant IDs:** requirements.md (FR-006, FR-007, AC-011, AC-012, AC-013, AC-014)
- **Completion Criteria:**
  - GET /api/v1/tickets?search={keyword} - Search tickets
  - GET /api/v1/tickets?status={status} - Filter by status
  - Combined: GET /api/v1/tickets?search={keyword}&status={status}
  - Search across title and description (case-insensitive)
  - Maximum search length: 100 characters (returns 400 if exceeded)
  - Invalid status returns 400

### Task 3.7: State Transition Logic
- **Task ID:** 3.7
- **Description:** Implement state machine enforcement
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/StateMachineService.java` (new file)
  - `src/main/java/org/c2/supportticketsystem/service/TicketService.java` (add method)
- **Dependencies/Prerequisites:** Tasks 1.3, 2.1, 2.2 completed
- **Relevant IDs:** requirements.md (Section 10 - State-Machine Requirements), state-machine.md
- **Completion Criteria:**
  - Valid transitions: OPEN->IN_PROGRESS, OPEN->CANCELLED, IN_PROGRESS->RESOLVED, IN_PROGRESS->CANCELLED, RESOLVED->CLOSED
  - Invalid transitions return 409 Conflict
  - Error response includes validTransitions list

---

## 4. API Layer

### Task 4.1: Request DTOs
- **Task ID:** 4.1
- **Description:** Create request DTO classes
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/dto/request/CreateTicketRequest.java`
  - `src/main/java/org/c2/supportticketsystem/dto/request/UpdateTicketRequest.java`
  - `src/main/java/org/c2/supportticketsystem/dto/request/AddCommentRequest.java`
  - `src/main/java/org/c2/supportticketsystem/dto/request/TransitionRequest.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.1, 2.2, 2.3 completed
- **Relevant IDs:** api-contract.md (Request DTOs), requirements.md (Validation Requirements)
- **Completion Criteria:**
  - All validation annotations (@NotBlank, @Size, @NotNull, etc.)
  - All fields match API contract specification
  - Assignee field allows null

### Task 4.2: Response DTOs
- **Task ID:** 4.2
- **Description:** Create response DTO classes
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/dto/response/TicketResponse.java`
  - `src/main/java/org/c2/supportticketsystem/dto/response/CommentResponse.java`
  - `src/main/java/org/c2/supportticketsystem/dto/response/TicketListResponse.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.2, 2.3, 2.5 completed
- **Relevant IDs:** api-contract.md (Response DTOs), architecture.md (DTO Pattern)
- **Completion Criteria:**
  - All fields match API contract specification
  - TicketListResponse includes pagination metadata
  - camelCase field naming

### Task 4.3: Controllers
- **Task ID:** 4.3
- **Description:** Create REST controllers
- **Files to Create:**
  - `src/main/java/org/c2/supportticketsystem/controller/TicketController.java`
  - `src/main/java/org/c2/supportticketsystem/controller/CommentController.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 3.1-3.7 completed
- **Relevant IDs:** architecture.md (Controller Layer), api-contract.md (All Endpoints)
- **Completion Criteria:**
  - All endpoints from API contract implemented
  - Controllers delegate to service layer (no business logic)
  - @Valid on request bodies
  - @PathVariable and @RequestParam for query parameters

### Task 4.4: Validation Configuration
- **Task ID:** 4.4
- **Description:** Configure validation and error handling
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/config/ValidationConfig.java`
  - `src/main/java/org/c2/supportticketsystem/exception/GlobalExceptionHandler.java`
- **Dependencies/Prerequisites:** Task 4.3 completed
- **Relevant IDs:** api-contract.md (Validation), rules/java-springboot.md (Validation), requirements.md (Error Handling)
- **Completion Criteria:**
  - @Valid annotation processing
  - 400 Bad Request with violations array
  - 404 Not Found with proper format
  - 409 Conflict with state transition errors

---

## 5. State Machine

### Task 5.1: State Machine Implementation
- **Task ID:** 5.1
- **Description:** Implement state transition validation logic
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/service/StateMachineService.java` (add validation)
  - `src/main/java/org/c2/supportticketsystem/exception/InvalidStateTransitionException.java`
- **Dependencies/Prerequisites:** Tasks 1.3, 2.1, 2.2, 3.7 completed
- **Relevant IDs:** state-machine.md (All sections), requirements.md (Section 10)
- **Completion Criteria:**
  - isOpenValidTransition() method with all valid transitions
  - getValidTransitions() returns list for current state
  - throws InvalidStateTransitionException with validTransitions on invalid transition

### Task 5.2: State Transition Controller Endpoint
- **Task ID:** 5.2
- **Description:** Implement transition endpoint with validation
- **Files to Create/Modify:**
  - `src/main/java/org/c2/supportticketsystem/controller/TicketController.java` (add endpoint)
- **Dependencies/Prerequisites:** Tasks 4.3, 5.1 completed
- **Relevant IDs:** state-machine.md (State Transition Endpoint), requirements.md (FR-008, AC-015, AC-016, AC-017)
- **Completion Criteria:**
  - POST /api/v1/tickets/{id}/transition endpoint
  - Returns 200 OK for valid transitions
  - Returns 400 Bad Request for invalid status value
  - Returns 409 Conflict for disallowed transitions with validTransitions in response

---

## 6. Frontend

### Task 6.1: Project Initialization
- **Task ID:** 6.1
- **Description:** Initialize Next.js project
- **Files to Create:**
  - `src/frontend/package.json` - Project configuration
  - `src/frontend/next.config.js` - Next.js configuration
  - `src/frontend/tsconfig.json` - TypeScript configuration
- **Dependencies/Prerequisites:** Node.js installed
- **Relevant IDs:** architecture.md (Frontend), ui-flow.md (Frontend Architecture)
- **Completion Criteria:**
  - `npm run dev` starts development server
  - Basic page renders
  - TypeScript configured

### Task 6.2: API Client Setup
- **Task ID:** 6.2
- **Description:** Create API client utilities
- **Files to Create:**
  - `src/frontend/lib/api/client.ts` - HTTP client with error handling
  - `src/frontend/lib/api/endpoints.ts` - API endpoint constants
- **Dependencies/Prerequisites:** Task 6.1 completed
- **Relevant IDs:** ui-flow.md (Frontend Architecture)
- **Completion Criteria:**
  - axios or fetch API configured
  - Base URL from environment variable
  - Error handling for 400, 404, 409, 500

### Task 6.3: Layout and Navigation
- **Task ID:** 6.3
- **Description:** Create main layout with navigation
- **Files to Create:**
  - `src/frontend/app/layout.tsx` - Root layout
  - `src/frontend/components/AppLayout.tsx` - Layout wrapper
- **Dependencies/Prerequisites:** Task 6.2 completed
- **Relevant IDs:** ui-flow.md (AppLayout)
- **Completion Criteria:**
  - Navigation links (Home, Create Ticket)
  - Error boundary component
  - Consistent header/navigation

### Task 6.4: Ticket List View
- **Task ID:** 6.4
- **Description:** Implement ticket list view
- **Files to Create:**
  - `src/frontend/app/page.tsx` - Home page (ticket list)
  - `src/frontend/components/TicketCard.tsx` - Ticket display component
- **Dependencies/Prerequisites:** Tasks 6.2, 6.3 completed
- **Relevant IDs:** ui-flow.md (Ticket List View), requirements.md (AC-003)
- **Completion Criteria:**
  - Paginated ticket cards
  - Displays: title, status, priority, createdAt
  - Search input
  - Status filter dropdown
  - Pagination controls

### Task 6.5: Ticket Creation Form
- **Task ID:** 6.5
- **Description:** Create ticket creation form
- **Files to Create:**
  - `src/frontend/app/tickets/new/page.tsx`
  - `src/frontend/components/TicketForm.tsx`
- **Dependencies/Prerequisites:** Tasks 6.2, 6.3 completed
- **Relevant IDs:** ui-flow.md (Create Ticket View), requirements.md (FR-001, AC-001, AC-002)
- **Completion Criteria:**
  - Title, description, priority, assignee inputs
  - Validation errors displayed inline
  - Submit creates ticket (POST /api/v1/tickets)
  - Redirects to ticket detail on success

### Task 6.6: Ticket Detail View
- **Task ID:** 6.6
- **Description:** Implement ticket detail view
- **Files to Create:**
  - `src/frontend/app/tickets/[id]/page.tsx`
  - `src/frontend/components/TicketDetail.tsx`
  - `src/frontend/components/CommentList.tsx`
- **Dependencies/Prerequisites:** Tasks 6.2, 6.3 completed
- **Relevant IDs:** ui-flow.md (Ticket Detail View), requirements.md (FR-003, AC-004, AC-010)
- **Completion Criteria:**
  - Ticket information display
  - Comments in chronological order
  - Status badge
  - Edit button

### Task 6.7: Edit Ticket Form
- **Task ID:** 6.7
- **Description:** Implement ticket edit form
- **Files to Create:**
  - `src/frontend/app/tickets/[id]/edit/page.tsx`
  - `src/frontend/components/TicketEditForm.tsx`
- **Dependencies/Prerequisites:** Tasks 6.5 completed
- **Relevant IDs:** ui-flow.md (Edit Ticket View), requirements.md (FR-004, AC-005, AC-006, AC-007)
- **Completion Criteria:**
  - Pre-filled form with ticket data
  - Partial update via PATCH
  - Status cannot be edited here
  - Validation errors displayed

### Task 6.8: Add Comment Form
- **Task ID:** 6.8
- **Description:** Implement comment addition form
- **Files to Create:**
  - `src/frontend/components/CommentForm.tsx`
- **Dependencies/Prerequisites:** Task 6.6 completed
- **Relevant IDs:** ui-flow.md (Ticket Detail View), requirements.md (FR-005, AC-008, AC-009)
- **Completion Criteria:**
  - Text and author inputs
  - POST /api/v1/tickets/{id}/comments
  - Validation errors displayed
  - Comments refresh after addition

### Task 6.9: Search and Filter UI
- **Task ID:** 6.9
- **Description:** Implement search and filter UI components
- **Files to Create/Modify:**
  - `src/frontend/app/page.tsx` (add search/filter)
  - `src/frontend/components/SearchFilter.tsx` (new component)
- **Dependencies/Prerequisites:** Task 6.4 completed
- **Relevant IDs:** ui-flow.md (Ticket List View), requirements.md (FR-006, FR-007)
- **Completion Criteria:**
  - Search input (max 100 chars)
  - Status filter dropdown
  - Combined search and filter
  - Validation error for search > 100 chars

### Task 6.10: State Transition UI
- **Task ID:** 6.10
- **Description:** Implement state transition controls
- **Files to Create/Modify:**
  - `src/frontend/components/StateTransitionControl.tsx`
  - `src/frontend/app/tickets/[id]/page.tsx` (add transition UI)
- **Dependencies/Prerequisites:** Task 6.6 completed
- **Relevant IDs:** ui-flow.md (State Transition UI Flow), requirements.md (FR-008, AC-015, AC-016, AC-017)
- **Completion Criteria:**
  - Transition dropdown with valid options
  - POST /api/v1/tickets/{id}/transition
  - Error display with valid transitions on 409

### Task 6.11: Error Display Components (FR-009)
- **Task ID:** 6.11
- **Description:** Implement error display components
- **Files to Create:**
  - `src/frontend/components/ErrorDisplay.tsx` - Generic error banner
  - `src/frontend/components/ValidationErrorBanner.tsx` - Validation errors
  - `src/frontend/components/TransitionErrorBanner.tsx` - Transition errors
  - `src/frontend/components/NotFoundErrorBanner.tsx` - 404 errors
- **Dependencies/Prerequisites:** Task 6.2 completed
- **Relevant IDs:** ui-flow.md (Error Handling), requirements.md (FR-009, AC-009a, AC-009b, AC-009c)
- **Completion Criteria:**
  - Validation errors displayed inline and in banner
  - State transition errors show valid transitions
  - 404 errors show navigation options
  - Errors don't block other UI interactions

---

## 7. Testing

### Task 7.1: Backend Unit Tests
- **Task ID:** 7.1
- **Description:** Write unit tests for service layer
- **Files to Create:**
  - `src/test/java/org/c2/supportticketsystem/unit/service/TicketServiceTest.java`
  - `src/test/java/org/c2/supportticketsystem/unit/service/CommentServiceTest.java`
  - `src/test/java/org/c2/supportticketsystem/unit/service/StateMachineServiceTest.java`
- **Dependencies/Prerequisites:** Tasks 3.1-3.7, 5.1 completed
- **Relevant IDs:** test-strategy.md (Unit Tests), requirements.md (Section 14)
- **Completion Criteria:**
  - All service methods tested
  - Validation exceptions verified
  - State machine transitions tested

### Task 7.2: Backend Validation Tests
- **Task ID:** 7.2
- **Description:** Write validation tests
- **Files to Create:**
  - `src/test/java/org/c2/supportticketsystem/unit/validation/RequestValidationTest.java`
- **Dependencies/Prerequisites:** Task 7.1 completed
- **Relevant IDs:** test-strategy.md (Validation Tests), requirements.md (Section 9)
- **Completion Criteria:**
  - All validation constraints tested
  - @NotBlank, @Size, @NotNull verified

### Task 7.3: Backend Integration Tests
- **Task ID:** 7.3
- **Description:** Write integration tests
- **Files to Create:**
  - `src/test/java/org/c2/supportticketsystem/integration/TicketServiceIntegrationTest.java`
  - `src/test/java/org/c2/supportticketsystem/integration/CommentServiceIntegrationTest.java`
- **Dependencies/Prerequisites:** Tasks 2.2-2.4 completed
- **Relevant IDs:** test-strategy.md (Integration Tests), requirements.md (Section 12)
- **Completion Criteria:**
  - Database persistence tested
  - Transactions verified
  - Timestamps set correctly

### Task 7.4: API/Controller Tests
- **Task ID:** 7.4
- **Description:** Write API contract tests
- **Files to Create:**
  - `src/test/java/org/c2/supportticketsystem/api/TicketControllerTest.java`
  - `src/test/java/org/c2/supportticketsystem/api/CommentControllerTest.java`
- **Dependencies/Prerequisites:** Task 4.3 completed
- **Relevant IDs:** test-strategy.md (API/Controller Tests), requirements.md (Section 14 - ACs)
- **Completion Criteria:**
  - All endpoints tested with MockMvc
  - All status codes verified (200, 201, 400, 404, 409)
  - Error response formats verified

### Task 7.5: State-Machine Tests
- **Task ID:** 7.5
- **Description:** Write state machine tests
- **Files to Create:**
  - `src/test/java/org/c2/supportticketsystem/unit/statemachine/StateTransitionTest.java`
- **Dependencies/Prerequisites:** Task 7.1 completed
- **Relevant IDs:** test-strategy.md (State-Machine Tests), requirements.md (Section 10), state-machine.md
- **Completion Criteria:**
  - All valid transitions tested
  - All invalid transitions return 409
  - Error response includes validTransitions

### Task 7.6: Persistence Tests
- **Task ID:** 7.6
- **Description:** Write persistence tests
- **Files to Create:**
  - `src/test/java/org/c2/supportticketsystem/integration/PersistenceTest.java`
- **Dependencies/Prerequisites:** Task 7.3 completed
- **Relevant IDs:** test-strategy.md (Persistence Tests), requirements.md (Section 12)
- **Completion Criteria:**
  - AC-018: Data persists across restart
  - AC-019: SQL injection prevention

### Task 7.7: Frontend Tests
- **Task ID:** 7.7
- **Description:** Write frontend tests
- **Files to Create:**
  - `src/frontend/__tests__/components/ErrorDisplay.test.tsx`
  - `src/frontend/__tests__/components/TicketForm.test.tsx`
  - `src/frontend/__tests__/components/StateTransition.test.tsx`
  - `src/frontend/__tests__/components/TicketList.test.tsx`
- **Dependencies/Prerequisites:** Task 6.10 completed
- **Relevant IDs:** test-strategy.md (UI Error Handling Tests), requirements.md (FR-009, AC-009a, AC-009b, AC-009c)
- **Completion Criteria:**
  - Error display tests for validation, transition, and 404 errors
  - Form validation tests
  - State transition UI tests

---

## Recommended Implementation Order

### Phase 1: Foundation (Week 1)
1. 1.1 Project Initialization
2. 1.2 Database Configuration
3. 1.3 Project Structure Setup
4. 2.1 Enumerations
5. 2.2 Ticket Entity
6. 2.3 Comment Entity

### Phase 2: Data Layer (Week 2)
7. 2.4 Repositories
8. 2.5 Mappers
9. 4.1 Request DTOs
10. 4.2 Response DTOs

### Phase 3: Business Logic (Week 3)
11. 3.1 Ticket Creation Service
12. 3.2 Ticket Listing with Pagination
13. 3.3 Ticket Details Retrieval
14. 3.4 Ticket Partial Update
15. 3.5 Comment Addition
16. 3.6 Search and Filter
17. 3.7 State Transition Logic

### Phase 4: API Layer (Week 4)
18. 4.3 Controllers
19. 4.4 Validation Configuration
20. 5.1 State Machine Implementation
21. 5.2 State Transition Controller Endpoint

### Phase 5: Frontend (Week 5-6)
22. 6.1 Project Initialization
23. 6.2 API Client Setup
24. 6.3 Layout and Navigation
25. 6.4 Ticket List View
26. 6.5 Ticket Creation Form
27. 6.6 Ticket Detail View
28. 6.7 Edit Ticket Form
29. 6.8 Add Comment Form
30. 6.9 Search and Filter UI
31. 6.10 State Transition UI
32. 6.11 Error Display Components (FR-009)

### Phase 6: Testing (Week 7-8)
33. 7.1 Backend Unit Tests
34. 7.2 Backend Validation Tests
35. 7.3 Backend Integration Tests
36. 7.4 API/Controller Tests
37. 7.5 State-Machine Tests
38. 7.6 Persistence Tests
39. 7.7 Frontend Tests

---

## Dependency Relationships

```
Phase 1 (Foundation)
├── 1.1 → 1.2 (Database config needs project structure)
├── 1.1 → 1.3 (Project structure needs project init)
└── 1.2 + 1.3 → 2.1 (Enums need database and structure)

Phase 2 (Data Layer)
├── 2.1 → 2.2 (Ticket needs Status enum)
├── 2.1 → 2.3 (Comment needs Status enum reference)
├── 2.2 + 2.3 → 2.4 (Repositories need entities)
├── 2.2 + 2.3 → 2.5 (Mappers need entities)
└── 2.4 → 3.x (All services need repositories)

Phase 3 (Business Logic)
├── 2.2 + 2.3 + 2.5 → 3.1 (Create needs entity and mapper)
├── 3.1 → 3.2 (List needs create for context)
├── 3.2 → 3.3 (Detail needs list context)
├── 3.3 → 3.4 (Update needs detail)
├── 2.2 + 2.3 → 3.5 (Comment needs entities)
├── 3.2 → 3.6 (Search/filter needs list)
└── 2.1 + 2.2 → 3.7 (State machine needs enums and ticket)

Phase 4 (API Layer)
├── 2.1 + 2.2 + 2.3 → 4.1 (Request DTOs need entities)
├── 2.2 + 2.3 + 2.5 → 4.2 (Response DTOs need entities and mappers)
├── 3.1-3.7 → 4.3 (Controllers need services)
├── 4.3 → 4.4 (Validation needs controllers)
├── 3.7 → 5.1 (State machine needs service logic)
└── 4.3 + 5.1 → 5.2 (Transition endpoint needs controller and state machine)

Phase 5 (Frontend)
├── 6.1 → 6.2 (API client needs project)
├── 6.2 → 6.3 (Layout needs API client for navigation)
├── 6.3 → 6.4 (Ticket list needs layout)
├── 6.2 → 6.5 (Create needs API client)
├── 6.2 → 6.6 (Detail needs API client)
├── 6.5 → 6.7 (Edit needs create for reference)
├── 6.2 + 6.6 → 6.8 (Comment needs API client and detail)
├── 6.4 → 6.9 (Search/filter needs list)
├── 6.6 → 6.10 (State transition needs detail)
└── 6.2 → 6.11 (Error display needs API client)

Phase 6 (Testing)
├── 3.1-3.7 → 7.1 (Unit tests need services)
├── 7.1 → 7.2 (Validation tests need service tests)
├── 2.2-2.4 → 7.3 (Integration tests need entities and repos)
├── 4.3 → 7.4 (API tests need controllers)
├── 7.1 → 7.5 (State machine tests need service tests)
├── 7.3 → 7.6 (Persistence tests need integration tests)
└── 6.10 → 7.7 (Frontend tests need UI components)
```

---

## Acceptance Criteria Checklist

| AC ID | Implementation Task | Test Task | Status |
|-------|--------------------|-----------|--------|
| AC-001 | 3.1 Ticket Creation | 7.1 TicketServiceTest | ✅ |
| AC-002 | 3.1 Ticket Creation | 7.2 RequestValidationTest | ✅ |
| AC-003 | 3.2 Ticket Listing | 7.4 TicketControllerTest | ✅ |
| AC-004 | 3.3 Ticket Details | 7.4 TicketControllerTest | ✅ |
| AC-005 | 3.4 Ticket Update | 7.1 TicketServiceTest | ✅ |
| AC-006 | 3.4 Ticket Update | 7.2 RequestValidationTest | ✅ |
| AC-007 | 3.4 Ticket Update | 7.4 TicketControllerTest | ✅ |
| AC-008 | 3.5 Comment Addition | 7.1 CommentServiceTest | ✅ |
| AC-009 | 3.5 Comment Addition | 7.4 CommentControllerTest | ✅ |
| AC-009a | 6.11 Error Display | 7.7 Frontend Error Tests | ✅ |
| AC-009b | 6.10 State Transition | 7.7 Frontend Error Tests | ✅ |
| AC-009c | 6.11 Error Display | 7.7 Frontend Error Tests | ✅ |
| AC-010 | 3.3 Ticket Details | 7.4 TicketControllerTest | ✅ |
| AC-011 | 3.6 Search and Filter | 7.4 TicketControllerTest | ✅ |
| AC-012 | 3.6 Search and Filter | 7.4 TicketControllerTest | ✅ |
| AC-013 | 3.6 Search and Filter | 7.4 TicketControllerTest | ✅ |
| AC-014 | 3.6 Search and Filter | 7.4 TicketControllerTest | ✅ |
| AC-015 | 5.2 State Transition Endpoint | 7.5 StateTransitionTest | ✅ |
| AC-016 | 5.2 State Transition Endpoint | 7.4 TicketControllerTest | ✅ |
| AC-017 | 5.2 State Transition Endpoint | 7.5 StateTransitionTest | ✅ |
| AC-018 | 7.6 Persistence Tests | 7.6 Persistence Tests | ✅ |
| AC-019 | 2.4 Repositories | 7.6 Persistence Tests | ✅ |

### State Machine Test Coverage

| State Machine Rule | Test Case | Status |
|--------------------|-----------|--------|
| SM-001: Initial state OPEN | TicketServiceTest.createTicket() | ✅ |
| SM-002-TC1: OPEN -> IN_PROGRESS | StateTransitionServiceTest | ✅ |
| SM-002-TC2: OPEN -> CANCELLED | StateTransitionServiceTest | ✅ |
| SM-003-TC1: IN_PROGRESS -> RESOLVED | StateTransitionServiceTest | ✅ |
| SM-003-TC2: IN_PROGRESS -> CANCELLED | StateTransitionServiceTest | ✅ |
| SM-004-TC1: RESOLVED -> CLOSED | StateTransitionServiceTest | ✅ |
| SM-005: CANCELLED terminal | StateTransitionServiceTest | ✅ |
| SM-006: CLOSED terminal | StateTransitionServiceTest | ✅ |
| SM-007: Invalid transitions | StateTransitionServiceTest | ✅ |
| SM-008: Error response | TicketControllerTest | ✅ |
| SM-009: Persistence | PersistenceTest | ✅ |

**All acceptance criteria have corresponding implementation and test tasks.**