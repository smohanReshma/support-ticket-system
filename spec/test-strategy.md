# Support Ticket Management System - Test Strategy

## 1. Overview

This document defines the test strategy for the Support Ticket Management System. Tests are derived from requirements.md (Section 14 - Acceptance Criteria) and follow the standards in `rules/testing.md`.

## 2. Test Categories

### 2.1 Test Matrix

| Test Type | Scope | Framework | Target Coverage |
|-----------|-------|-----------|-----------------|
| Unit Tests | Individual classes | JUnit 5 + Mockito | Business logic, validation |
| Integration Tests | Component interactions | Spring Boot Test | Service layer, repositories |
| API/Controller Tests | REST endpoints | MockMvc | All endpoints |
| State-Machine Tests | State transitions | JUnit 5 + Spring | All valid/invalid transitions |
| Validation Tests | Bean validation | Hibernate Validator | All validation rules |
| Persistence Tests | Database operations | Spring Data JPA | CRUD, constraints |
| UI Error Handling Tests | Frontend error display | Jest + React Testing Library | FR-009 requirements |

## 3. Unit Tests

### 3.1 Service Layer Tests

**Target Classes:** `TicketService`, `CommentService`, `StateTransitionService`

**Test Cases:**

| Test Scenario | Method | Expected |
|--------------|--------|----------|
| Create valid ticket | createTicket() | Ticket created with OPEN status |
| Create ticket with blank title | createTicket() | Validation exception |
| Create ticket with blank description | createTicket() | Validation exception |
| Update ticket fields | updateTicket() | Fields updated, status unchanged |
| Invalid state transition | transitionStatus() | InvalidStateTransitionException |
| Valid state transition | transitionStatus() | Status updated, updatedAt changed |

**Example:**
```java
@Test
void createTicket_ValidRequest_CreatesTicket() {
    // Given
    CreateTicketRequest request = new CreateTicketRequest(
        "Test title", "Test description", Priority.HIGH, null);
    
    // When
    TicketResponse response = ticketService.createTicket(request);
    
    // Then
    assertNotNull(response.getId());
    assertEquals(Status.OPEN, response.getStatus());
    assertEquals("Test title", response.getTitle());
}

@Test
void createTicket_BlankTitle_ThrowsValidationException() {
    // Given
    CreateTicketRequest request = new CreateTicketRequest(
        "", "Test description", Priority.HIGH, null);
    
    // When/Then
    assertThrows(ValidationException.class, () -> 
        ticketService.createTicket(request));
}
```

### 3.2 Mapper Tests

**Target Classes:** `TicketMapper`, `CommentMapper`

**Test Cases:**
- Entity to DTO conversion preserves all fields
- DTO to entity conversion works for create/update operations
- Null handling for optional fields

### 3.3 State Machine Tests

**Target Classes:** `StateMachineService` (or validation methods in `TicketService`)

**Test Cases:**

| Current State | Target State | Expected Result |
|---------------|--------------|-----------------|
| OPEN | IN_PROGRESS | Valid |
| OPEN | CANCELLED | Valid |
| OPEN | RESOLVED | Invalid (409) |
| OPEN | CLOSED | Invalid (409) |
| OPEN | OPEN | Invalid (409) |
| IN_PROGRESS | RESOLVED | Valid |
| IN_PROGRESS | CANCELLED | Valid |
| IN_PROGRESS | OPEN | Invalid (409) |
| IN_PROGRESS | IN_PROGRESS | Invalid (409) |
| RESOLVED | CLOSED | Valid |
| RESOLVED | OPEN | Invalid (409) |
| RESOLVED | IN_PROGRESS | Invalid (409) |
| RESOLVED | RESOLVED | Invalid (409) |
| RESOLVED | CANCELLED | Invalid (409) |
| CANCELLED | Any | Invalid (409) |
| CLOSED | Any | Invalid (409) |

**Example:**
```java
@Test
void transition_Open_To_InProgress_Valid() {
    // Given: ticket in OPEN state
    Ticket ticket = createTicket(Status.OPEN);
    
    // When: transition to IN_PROGRESS
    Ticket result = stateMachineService.transitionStatus(ticket.getId(), Status.IN_PROGRESS);
    
    // Then: success, status is IN_PROGRESS
    assertEquals(Status.IN_PROGRESS, result.getStatus());
}

@Test
void transition_Open_To_Resolved_Invalid() {
    // Given: ticket in OPEN state
    Ticket ticket = createTicket(Status.OPEN);
    
    // When: transition to RESOLVED (bypasses IN_PROGRESS)
    InvalidStateTransitionException exception = assertThrows(
        InvalidStateTransitionException.class, 
        () -> stateMachineService.transitionStatus(ticket.getId(), Status.RESOLVED)
    );
    
    // Then: 409 Conflict, status remains OPEN
    assertEquals(Status.OPEN, ticket.getStatus());
    assertTrue(exception.getValidTransitions().contains(Status.IN_PROGRESS));
    assertTrue(exception.getValidTransitions().contains(Status.CANCELLED));
}
```

## 4. Integration Tests

### 4.1 Service Layer Integration Tests

**Target:** Service layer with database interaction

**Configuration:**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Transactional
class TicketServiceIntegrationTest {
    // Tests service layer with real database
}
```

**Test Cases:**
- Create ticket persists to database
- Ticket retrieval returns correct data
- State transitions persist correctly
- Comments are added and retrieved with tickets
- Timestamps are set correctly

**Example:**
```java
@Test
void createTicket_PersistsToDatabase() {
    // Given: no tickets in database
    assertEquals(0, ticketRepository.count());
    
    // When: create ticket
    CreateTicketRequest request = new CreateTicketRequest(
        "Test", "Test desc", Priority.MEDIUM, null);
    ticketService.createTicket(request);
    
    // Then: ticket exists in database
    assertEquals(1, ticketRepository.count());
    Ticket saved = ticketRepository.findAll().get(0);
    assertEquals(Status.OPEN, saved.getStatus());
}
```

### 4.2 Repository Tests

**Configuration:**
```java
@DataJpaTest
class TicketRepositoryTest {
    // Tests repository methods with in-memory database
}
```

**Test Cases:**
- findAll() returns all tickets
- findById() returns ticket by ID
- save() persists ticket
- findByStatus() filters by status
- search() performs case-insensitive search
- Pagination works correctly

**Example:**
```java
@Test
void search_CaseInsensitive_FindsMatches() {
    // Given: ticket with title "Login Issue"
    ticketRepository.save(new Ticket("Login Issue", "Test", Priority.HIGH));
    
    // When: search for "login"
    List<Ticket> results = ticketRepository.search("login", Sort.by("createdAt").descending());
    
    // Then: returns the ticket
    assertEquals(1, results.size());
    assertEquals("Login Issue", results.get(0).getTitle());
}
```

## 5. API/Controller Tests

### 5.1 Configuration

```java
@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {
    @Autowired
    private MockMvc mockMvc;
}
```

### 5.2 Test Endpoints

#### 5.2.1 POST /api/v1/tickets (Create)

**Success:**
- Returns 201 Created with correct response body
- Response includes id, createdAt, status OPEN

**Validation Errors:**
- Returns 400 Bad Request with violations array
- Blank title, blank description, invalid priority

**Example:**
```java
@Test
void createTicket_ValidRequest_Returns201() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest(
        "Test", "Test desc", Priority.HIGH, null);
    
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.status").value("OPEN"));
}

@Test
void createTicket_BlankTitle_Returns400() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest(
        "", "Test desc", Priority.HIGH, null);
    
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations[0].field").value("title"));
}
```

#### 5.2.2 GET /api/v1/tickets (List)

**Success:**
- Returns 200 OK with paginated response
- Response includes content, page, size, totalElements, totalPages

**Example:**
```java
@Test
void listTickets_ReturnsPaginatedResult() throws Exception {
    // Given: 25 tickets in database
    
    mockMvc.perform(get("/api/v1/tickets?page=0&size=20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(20))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(25))
        .andExpect(jsonPath("$.totalPages").value(2));
}
```

#### 5.2.3 GET /api/v1/tickets/{id} (Detail)

**Success:**
- Returns 200 OK with complete ticket details

**Not Found:**
- Returns 404 Not Found

**Example:**
```java
@Test
void getTicket_NonExistentId_Returns404() throws Exception {
    mockMvc.perform(get("/api/v1/tickets/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
}
```

#### 5.2.4 PATCH /api/v1/tickets/{id} (Update)

**Success:**
- Returns 200 OK with updated ticket

**Validation Errors:**
- Returns 400 Bad Request with violations

**Example:**
```java
@Test
void updateTicket_PartialUpdate_UpdatesOnlyFields() throws Exception {
    UpdateTicketRequest request = new UpdateTicketRequest(
        "Updated title", null, null, "new-assignee@example.com");
    
    mockMvc.perform(patch("/api/v1/tickets/1")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated title"))
        .andExpect(jsonPath("$.assignee").value("new-assignee@example.com"))
        .andExpect(jsonPath("$.description").doesNotExist()); // unchanged
}
```

#### 5.2.5 POST /api/v1/tickets/{id}/transition (State Transition)

**Success:**
- Returns 200 OK with updated ticket

**Invalid Transition:**
- Returns 409 Conflict with valid transitions

**Example:**
```java
@Test
void transitionStatus_InvalidTransition_Returns409() throws Exception {
    TransitionRequest request = new TransitionRequest(Status.RESOLVED);
    
    mockMvc.perform(post("/api/v1/tickets/1/transition")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.currentStatus").value("OPEN"))
        .andExpect(jsonPath("$.requestedStatus").value("RESOLVED"))
        .andExpect(jsonPath("$.validTransitions").isArray())
        .andExpect(jsonPath("$.validTransitions").value(containsInAnyOrder(
            "IN_PROGRESS", "CANCELLED")));
}
```

#### 5.2.6 POST /api/v1/tickets/{id}/comments (Add Comment)

**Success:**
- Returns 201 Created with comment

**Validation Errors:**
- Returns 400 Bad Request with violations

**Example:**
```java
@Test
void addComment_ValidRequest_Returns201() throws Exception {
    AddCommentRequest request = new AddCommentRequest(
        "Test comment", "author@example.com");
    
    mockMvc.perform(post("/api/v1/tickets/1/comments")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.text").value("Test comment"));
}
```

## 6. Validation Tests

### 6.1 Bean Validation Tests

**Test Cases:**
- @NotBlank validation on required fields
- @Size validation on string fields
- @NotNull validation on enum fields
- Custom validation for assignee (null allowed, non-empty if provided)

**Example:**
```java
@Test
void createTicketRequest_TitleNotBlank() {
    CreateTicketRequest request = new CreateTicketRequest(
        "", "Test", Priority.HIGH, null);
    
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
    
    assertEquals(1, violations.size());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
}

@Test
void createTicketRequest_AssigneeNullAllowed() {
    CreateTicketRequest request = new CreateTicketRequest(
        "Test", "Test", Priority.HIGH, null);
    
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
    
    assertEquals(0, violations.size()); // null is valid
}
```

## 7. Persistence Tests

### 7.1 Transactional Tests

**Test Cases:**
- State transitions are transactional
- Failed updates don't persist partial changes
- Timestamps are updated correctly

**Example:**
```java
@Test
@Transactional
void updateTicket_FailedTransaction_DoesNotPersist() {
    // Given: ticket in database
    
    // When: update throws exception (e.g., constraint violation)
    assertThrows(DataIntegrityViolationException.class, () -> {
        // Attempt to violate constraint
    });
    
    // Then: original ticket unchanged
    Ticket original = ticketRepository.findById(ticketId).get();
    assertEquals("Original title", original.getTitle());
}
```

### 7.2 Constraint Tests

**Test Cases:**
- Unique constraints on primary keys
- NOT NULL constraints on required fields
- CHECK constraints on enum values
- Foreign key constraints on comments

**Example:**
```java
@Test
void ticket_StatusCheckConstraint_OnlyValidValues() {
    Ticket ticket = new Ticket("Test", "Test", Priority.HIGH, null);
    ticket.setStatus((Status) null); // Invalid
    
    assertThrows(DataIntegrityViolationException.class, () -> {
        ticketRepository.save(ticket);
    });
}
```

## 8. UI Error Handling Tests

### 8.1 Validation Error Display Tests

**Target:** React components displaying validation errors

**Test Cases:**
- Validation errors displayed inline for each field
- Validation errors displayed in summary banner
- Errors clear when input is corrected
- Errors do not block other UI interactions

**Example (React Testing Library):**
```typescript
test('displays validation error for blank title', () => {
    render(<TicketForm />);
    
    // Submit form with blank title
    fireEvent.click(screen.getByText('Create Ticket'));
    
    // Error should be displayed
    expect(screen.getByText('Title must not be blank')).toBeInTheDocument();
    expect(screen.getByDisplayValue('')).toHaveClass('error-border');
});

test('clears validation error when input is corrected', () => {
    render(<TicketForm />);
    
    // Submit form with blank title
    fireEvent.click(screen.getByText('Create Ticket'));
    
    // Error should be displayed
    expect(screen.getByText('Title must not be blank')).toBeInTheDocument();
    
    // Enter valid title
    fireEvent.change(screen.getByLabelText('Title'), {
        target: { value: 'Valid Title' }
    });
    
    // Error should be cleared
    expect(screen.queryByText('Title must not be blank')).not.toBeInTheDocument();
});
```

### 8.2 State Transition Error Display Tests

**Test Cases:**
- Invalid state transition shows error banner
- Error shows current state and valid transitions
- User can select valid transition after seeing error
- Error does not block other UI interactions

**Example:**
```typescript
test('shows state transition error with valid options', async () => {
    render(<TicketDetailView currentStatus="OPEN" />);
    
    // Attempt invalid transition
    await act(async () => {
        fireEvent.change(screen.getByRole('combobox'), {
            target: { value: 'RESOLVED' }
        });
        fireEvent.click(screen.getByText('Transition'));
    });
    
    // Error should be displayed with valid options
    expect(screen.getByText('Cannot transition to RESOLVED')).toBeInTheDocument();
    expect(screen.getByText('Valid next states: IN_PROGRESS, CANCELLED')).toBeInTheDocument();
    
    // Valid options should be available for selection
    expect(screen.getByRole('option', { name: 'IN_PROGRESS' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'CANCELLED' })).toBeInTheDocument();
});
```

### 8.3 404 Error Display Tests

**Test Cases:**
- 404 error shown for non-existent ticket
- Error shows resource type and ID
- Navigation options provided (go back, go to home)

**Example:**
```typescript
test('shows 404 error for non-existent ticket', async () => {
    render(<TicketDetailView ticketId={999} />);
    
    // Error should be displayed
    expect(screen.getByText('Resource not found')).toBeInTheDocument();
    expect(screen.getByText('Ticket ID: 999')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Go to home' })).toBeInTheDocument();
});
```

### 8.4 Network Error Display Tests

**Test Cases:**
- Network error shown for failed requests
- Retry option provided
- Error does not block other UI interactions

**Example:**
```typescript
test('shows network error with retry option', async () => {
    // Mock failed API call
    vi.spyOn(api, 'getTickets').mockRejectedValue(new Error('Network error'));
    
    render(<TicketListView />);
    
    // Error should be displayed
    expect(screen.getByText('An error occurred')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
});
```

## 9. Test Quality Metrics

### 9.1 Coverage Focus

- **Priority 1:** All acceptance criteria from requirements.md
- **Priority 2:** All state machine transitions (valid and invalid)
- **Priority 3:** All validation rules
- **Priority 4:** Error response formats

### 9.2 Test Naming Convention

```
{MethodName}_{StateUnderTest}_{ExpectedBehavior}()
```

Examples:
- `createTicket_ValidRequest_CreatesTicket()`
- `transitionStatus_OpenToResolved_InvalidTransition_Throws409()`
- `addComment_BlankText_Returns400()`
- `Search_CaseInsensitive_FindsMatches()`

### 9.3 Test Organization

```
src/test/java/org/c2/supportticketsystem/
├── unit/                           # Unit tests
│   ├── service/
│   │   ├── TicketServiceTest.java
│   │   ├── StateTransitionServiceTest.java
│   │   └── CommentServiceTest.java
│   ├── repository/
│   │   ├── TicketRepositoryTest.java
│   │   └── CommentRepositoryTest.java
│   ├── mapper/
│   │   ├── TicketMapperTest.java
│   │   └── CommentMapperTest.java
│   └── validation/
│       ├── TicketValidationTest.java
│       └── StateMachineValidationTest.java
├── integration/                    # Integration tests
│   ├── service/
│   │   └── TicketServiceIntegrationTest.java
│   └── repository/
│       └── TicketRepositoryIntegrationTest.java
├── api/                            # API/Controller tests
│   └── controller/
│       └── TicketControllerTest.java
└── ui/                             # UI error handling tests
    └── components/
        ├── ErrorDisplay.test.tsx
        ├── TicketForm.test.tsx
        ├── StateTransition.test.tsx
        └── TicketList.test.tsx
```

## 10. Test Execution Strategy

### 10.1 Local Development

```bash
# Run all tests
./gradlew test

# Run unit tests only
./gradlew test --tests "org.c2.supportticketsystem.unit.*"

# Run integration tests only
./gradlew testIntegration

# Run specific test class
./gradlew test --tests "TicketServiceTest"
```

### 10.2 CI/CD Pipeline

```yaml
# .github/workflows/test.yml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: ./gradlew test
```

## 11. Requirements Traceability

### 11.1 Test Mapping to Acceptance Criteria

| AC ID | Test Type | Test File |
|-------|-----------|-----------|
| AC-001 | Unit/Integration | TicketServiceTest.createTicket_ValidRequest() |
| AC-002 | Unit | TicketServiceTest.createTicket_BlankTitle() |
| AC-003 | API | TicketControllerTest.listTickets_ReturnsPaginatedResult() |
| AC-004 | API | TicketControllerTest.getTicket_DetailReturnsComments() |
| AC-005 | Unit/Integration | TicketServiceTest.updateTicket_PartialUpdate() |
| AC-006 | Unit | TicketServiceTest.updateTicket_InvalidField() |
| AC-007 | API | TicketControllerTest.updateTicket_NonExistentId() |
| AC-008 | Unit/Integration | CommentServiceTest.addComment_ValidRequest() |
| AC-009 | API | TicketControllerTest.addComment_NonExistentTicket() |
| AC-009a | UI | ErrorDisplay.test_validationErrorsDisplayed() |
| AC-009b | UI | StateTransition.test_transitionErrorsDisplayed() |
| AC-009c | UI | ErrorDisplay.test_notFoundErrorsDisplayed() |
| AC-010 | API | TicketControllerTest.getTicket_CommentsChronologicalOrder() |
| AC-011 | API | TicketControllerTest.searchTickets_KeywordMatch() |
| AC-012 | API | TicketControllerTest.filterTickets_ByStatus() |
| AC-013 | API | TicketControllerTest.filterTickets_InvalidStatus() |
| AC-014 | API | TicketControllerTest.searchAndFilter_Combined() |
| AC-015 | Unit | StateTransitionServiceTest.transition_OpenToInProgress_Valid() |
| AC-016 | API | TicketControllerTest.transitionStatus_InvalidStatus() |
| AC-017 | API | TicketControllerTest.transitionStatus_InvalidTransition() |
| AC-018 | Persistence | TicketRepositoryIntegrationTest.persistence_Restarts() |
| AC-019 | Integration | TicketRepositoryIntegrationTest.search_SqlInjectionPrevention() |

### 11.2 State-Machine Test Coverage

| State Machine Rule | Test Case | File |
|--------------------|-----------|------|
| SM-001: Initial state OPEN | TicketServiceTest.createTicket_DefaultOpenStatus() |
| SM-002: Valid OPEN transitions | StateTransitionServiceTest.transition_OpenToInProgress_Valid() |
| SM-002: Valid OPEN transitions | StateTransitionServiceTest.transition_OpenToCancelled_Valid() |
| SM-003: Valid IN_PROGRESS transitions | StateTransitionServiceTest.transition_InProgressToResolved_Valid() |
| SM-003: Valid IN_PROGRESS transitions | StateTransitionServiceTest.transition_InProgressToCancelled_Valid() |
| SM-004: Valid RESOLVED transitions | StateTransitionServiceTest.transition_ResolvedToClosed_Valid() |
| SM-005: CANCELLED terminal | StateTransitionServiceTest.transition_CancelledToAny_Invalid() |
| SM-006: CLOSED terminal | StateTransitionServiceTest.transition_ClosedToAny_Invalid() |
| SM-007: All invalid transitions | StateTransitionServiceTest.transition_OpenToResolved_Invalid() |
| SM-008: Error response | TicketControllerTest.transitionStatus_InvalidTransition_409Response() |
| SM-009: Persistence | TicketRepositoryIntegrationTest.transition_PersistsToDatabase() |
