# Generate Tests Command

Generate test files based on the specifications and acceptance criteria.

## Test Generation Mandate

**Tests must be derived from specifications and acceptance criteria. Include negative cases and invalid state transitions.**

## Test Generation Process

### 1. Start from Specification

- Identify all features from the spec
- For each feature, identify all acceptance criteria
- For each acceptance criterion, create test cases

### 2. Test Categories by Layer

#### Repository Tests
- Test CRUD operations for each entity
- Test custom query methods
- Test relationship loading
- Test transaction boundaries
- Test constraint violations

#### Service Tests
- Test business logic for each service method
- Test state machine transitions
- Test validation logic
- Test error handling
- Test transaction boundaries

#### Controller Tests
- Test all API endpoints
- Test request validation
- Test response status codes
- Test error responses
- Test authentication/authorization if applicable

#### Integration Tests
- Test full request flow
- Test database persistence
- Test service-layer business logic
- Test transaction rollback on errors

### 3. Test Case Types

#### Positive Tests (Happy Path)
- Test all valid operations
- Verify correct behavior
- Verify correct responses

#### Negative Tests (Error Cases)
- Test invalid input data
- Test unauthorized access
- Test resource not found
- Test validation failures
- Test conflict scenarios

#### Edge Case Tests
- Test boundary conditions
- Test empty collections
- Test null values where allowed
- Test maximum values
- Test concurrent access

#### State-Machine Tests

**For each state, test:**

1. All valid transitions FROM that state
2. All invalid transitions FROM that state
3. State remains unchanged on invalid transition
4. Proper error response with valid transitions listed

**Example structure:**
```java
@Test
void transition_ValidTransition_Success() {
    // Given: entity in state X
    // When: transition to state Y (valid)
    // Then: state is Y, success response
}

@Test
void transition_InvalidTransition_Conflict() {
    // Given: entity in state X
    // When: transition to state Z (invalid)
    // Then: 409 Conflict, state remains X
}
```

### 4. Test Generation Rules

#### Naming Convention
```
testMethodName_StateUnderTest_ExpectedBehavior()
```

Examples:
- `createTicket_ValidRequest_CreatesTicket()`
- `updateStatus_OpenToInProgress_ValidTransition_UpdatesStatus()`
- `updateStatus_OpenToResolved_InvalidTransition_ThrowsException()`
- `findTicketById_NonExistentId_ReturnsEmpty()`
- `validateEmail_ValidEmail_PassesValidation()`
- `validateEmail_InvalidEmail_ThrowsException()`
- `transition_CreateTicketStatus_ValidTransition_SetsStatus()`
- `transition_InvalidStateTransition_ConflictReturned()`

#### Test Structure
```java
@Test
void testMethodName_StateUnderTest_ExpectedBehavior() {
    // Given: setup test data
    // When: execute operation
    // Then: verify results
}
```

### 5. State-Machine Test Requirements

**For the ticket state machine:**

```java
// Valid transitions from OPEN
@Test void open_toInProgress_Valid();
@Test void open_toCancelled_Valid();
@Test void open_toResolved_Invalid();
@Test void open_toClosed_Invalid();

// Valid transitions from IN_PROGRESS
@Test void inProgress_toResolved_Valid();
@Test void inProgress_toCancelled_Valid();
@Test void inProgress_toOpen_Invalid();
@Test void inProgress_toClosed_Invalid();

// Valid transitions from RESOLVED
@Test void resolved_toClosed_Valid();
@Test void resolved_toOpen_Invalid();
@Test void resolved_toInProgress_Invalid();

// Valid transitions from CANCELLED
@Test void cancelled_toOpen_Invalid();
@Test void cancelled_toInProgress_Invalid();
@Test void cancelled_toResolved_Invalid();
@Test void cancelled_toClosed_Invalid();

// Valid transitions from CLOSED
@Test void closed_toOpen_Invalid();
@Test void closed_toInProgress_Invalid();
@Test void closed_toResolved_Invalid();
@Test void closed_toCancelled_Invalid();
```

### 6. Negative Test Requirements

For every positive test, create corresponding negative tests:

- Invalid input data
- Missing required fields
- Invalid field values
- Invalid state transitions
- Unauthorized access attempts
- Malformed requests

### 7. Validation Test Requirements

Test all validation rules:

- Required fields
- Size constraints
- Format constraints (email, URL, etc.)
- Custom validation rules
- Cross-field validation

### 8. API Test Requirements

For each API endpoint:

- Test successful operation
- Test validation errors
- Test not found errors
- Test conflict errors
- Test unauthorized errors
- Test forbidden errors
- Test all query parameters
- Test all path parameters

### 9. Persistence Test Requirements

- Test data is persisted correctly
- Test data is not persisted on error
- Test data survives restart (integration test)
- Test relationships are preserved
- Test constraints are enforced

### 10. Test File Organization

#### File Naming
- `{ClassName}Test.java` for unit tests
- `{ClassName}IT.java` for integration tests
- `{ClassName}ControllerTest.java` for controller tests
- `{EntityName}RepositoryTest.java` for repository tests
- `{ServiceName}Test.java` for service tests

#### Package Structure
- Test classes go in same package as source
- Or in subpackage: `test`, `integration`, `controller`

### 11. Test Data Setup

- Use `@BeforeEach` for common setup
- Use `@AfterEach` for cleanup if needed
- Create test fixtures for common data
- Clean up test data in `@AfterEach`

### 12. Mocking and Spying

- Use `@MockBean` for Spring dependencies
- Use `@Mock` for non-Spring collaborators
- Use `@Spy` when partial mocking needed

### 13. Database Testing

- Use in-memory database for tests
- Use `@AutoConfigureTestDatabase` to replace auto-configured database
- Clean database between tests
- Test transaction rollback

### 14. Final Checklist

Before finalizing tests:

- [ ] All acceptance criteria have tests
- [ ] All edge cases have tests
- [ ] All error scenarios have tests
- [ ] All state transitions have tests (valid and invalid)
- [ ] Negative tests exist for all positive tests
- [ ] Test names follow naming convention
- [ ] Tests are independent and can run in any order
- [ ] Tests clean up after themselves

### 15. Example Test Output

```java
package org.c2.supportticketsystem.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionSystemException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    @Test
    void createTicket_ValidRequest_CreatesTicket() {
        // Given
        var request = new CreateTicketRequest(
            "Test Ticket", "Test Description", Priority.MEDIUM, null);

        // When
        var result = ticketService.createTicket(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Ticket");
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    void createTicket_BlankTitle_ThrowsValidationException() {
        // Given
        var request = new CreateTicketRequest(
            "", "Test Description", Priority.MEDIUM, null);

        // Then
        assertThatThrownBy(() -> ticketService.createTicket(request))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void transition_OpenToInProgress_Valid() {
        // Given
        var ticket = ticketRepository.save(new Ticket(
            "Test", "Desc", Priority.MEDIUM, Status.OPEN, null));

        // When
        ticketService.transitionStatus(ticket.getId(), Status.IN_PROGRESS);

        // Then
        var updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void transition_OpenToResolved_Invalid_ThrowsConflict() {
        // Given
        var ticket = ticketRepository.save(new Ticket(
            "Test", "Desc", Priority.MEDIUM, Status.OPEN, null));

        // Then
        assertThatThrownBy(() -> ticketService.transitionStatus(
            ticket.getId(), Status.RESOLVED))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessageContaining("OPEN")
            .hasMessageContaining("RESOLVED");
    }
}
```