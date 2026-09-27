# Testing Standards

## Test Categories

### 1. Unit Tests
- Test individual classes in isolation
- Use JUnit 5 assertions
- Use Mockito for mocking dependencies
- Fast execution (no I/O, no Spring context)
- Test private methods indirectly through public API

### 2. Integration Tests
- Test component interactions
- Use `@SpringBootTest` for full context loading
- Test database interactions
- Use `@AutoConfigureTestDatabase` for in-memory database
- Test service-layer business logic

### 3. API/Controller Tests
- Use `MockMvc` for testing REST endpoints
- Test all HTTP methods (GET, POST, PUT, DELETE, PATCH)
- Test all status codes (200, 201, 400, 404, 409, etc.)
- Test request validation
- Test authentication/authorization if applicable

### 4. Repository Tests
- Test CRUD operations
- Test custom query methods
- Test relationships
- Test transaction boundaries
- Use `@DataJpaTest` for focused repository testing

### 5. Validation Tests
- Test Bean Validation annotations
- Test custom validation logic
- Test validation error messages
- Test nested object validation

### 6. State-Machine Tests
- Test all valid state transitions
- Test all invalid state transitions
- Verify state change is persisted
- Test concurrent state transition attempts
- Test state machine rules are enforced

### 7. Persistence/Restart Testing
- Test data persistence across application restarts
- Test entity relationships are preserved
- Test lazy/eager loading
- Test cascade operations
- Test database constraints are enforced

### 8. Negative Tests
- Test invalid input data
- Test unauthorized access
- Test resource not found
- Test conflict scenarios
- Test malformed requests

## Test Quality

### Do NOT prioritize arbitrary coverage percentages
- Focus on **quality of tests**, not quantity
- Tests that verify business rules and edge cases are more valuable than random coverage
- A well-tested critical path with 60% coverage is better than a poorly-tested 90% coverage
- Test behavior, not implementation details

### Test Derivation
- Tests must be derived from **specifications and acceptance criteria**
- Each acceptance criterion should have corresponding tests
- Each edge case from specification should have tests
- Each negative scenario should have tests

## Test Naming Convention

```java
testMethodName_StateUnderTest_ExpectedBehavior()
```

Examples:
- `createTicket_ValidRequest_CreatesTicket()`
- `updateStatus_OpenToInProgress_ValidTransition_UpdatesStatus()`
- `updateStatus_OpenToResolved_InvalidTransition_ThrowsException()`
- `findTicketById_NonExistentId_ReturnsEmpty()`
- `validateEmail_ValidEmail_PassesValidation()`
- `validateEmail_InvalidEmail_ThrowsException()`

## Test Structure

```java
class {ClassName}Test {

    // Fields (repositories, services, mock objects)

    @BeforeEach
    void setUp() {
        // Test setup
    }

    @Test
    void testMethodName_StateUnderTest_ExpectedBehavior() {
        // Given: prepare test data and fixtures
        // When: execute the method under test
        // Then: verify the results
    }
}
```

## Test Data Management

- Use `@BeforeEach` for common test setup
- Use `@AfterEach` for cleanup if needed
- Use TestConfiguration for test-specific beans
- Clean up test data after tests (especially integration tests)
- Use database transactions that roll back after each test

## State-Machine Testing Requirements

### Valid Transitions
- Test each valid transition from spec
- Verify state is updated in database
- Verify timestamp updates if applicable
- Verify audit trail if applicable

### Invalid Transitions
- Test each invalid transition from spec
- Verify `409 Conflict` is returned
- Verify state remains unchanged
- Verify error message is clear and helpful

### Example State-Machine Test Structure
```java
@Test
void transition_Open_To_InProgress_Valid() {
    // Given: ticket in OPEN state
    // When: transition to IN_PROGRESS
    // Then: success, status is IN_PROGRESS
}

@Test
void transition_Open_To_Resolved_Invalid() {
    // Given: ticket in OPEN state
    // When: transition to RESOLVED (bypasses IN_PROGRESS)
    // Then: 409 Conflict, status remains OPEN
}
```