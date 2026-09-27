# Code Review Command

Run code review on the current or specified file(s).

## Reviewer Mandate

**You must actively find defects and incorrect AI assumptions. You must NOT simply report that the code "looks good."**

## Review Checklist

### 1. Implementation vs Specification Compliance
- [ ] Does the implementation match the specification exactly?
- [ ] Are there any gaps between spec requirements and implementation?
- [ ] Are there any extra features not in the specification?
- [ ] Does the implementation handle all edge cases mentioned in the spec?

### 2. Code Quality and Structure
- [ ] Follows project structure and naming conventions
- [ ] Uses proper Spring annotations (@Service, @Repository, @RestController)
- [ ] Constructor injection is used (not field injection)
- [ ] No hardcoded values (all use properties/environment)
- [ ] Proper error handling with appropriate exceptions
- [ ] No println statements or debug code

### 3. Layered Architecture Compliance
- [ ] **Controller layer**: Only handles HTTP, no business logic
- [ ] **Service layer**: Contains business logic and state machine
- [ ] **Repository layer**: Only data access, no business logic
- [ ] No circular dependencies between layers

### 4. DTO and Entity Separation
- [ ] Entities are NEVER returned directly from controllers
- [ ] DTOs are used for all API requests/responses
- [ ] DTO-Entity mapping is correct and complete
- [ ] Validation is applied to DTOs, not entities

### 5. Validation
- [ ] Request validation with `@Valid` is present where needed
- [ ] Bean Validation annotations are appropriate
- [ ] Custom validation is implemented when needed
- [ ] Validation errors return `400 Bad Request` with details

### 6. State Machine Enforcement (CRITICAL)
- [ ] State transitions are **NOT** allowed via direct PATCH/PUT on status field
- [ ] State transitions use a dedicated `/transition` endpoint
- [ ] Backend validates ALL state transitions
- [ ] Invalid transitions return `409 Conflict`
- [ ] Error message includes valid transitions for current state
- [ ] State machine logic is in service layer, not controller

### 7. Transaction Management
- [ ] `@Transactional` used appropriately on service methods
- [ ] Read-only transactions for read operations
- [ ] Transaction boundaries are correct

### 8. Exception Handling
- [ ] Custom exceptions exist for domain errors
- [ ] `@ControllerAdvice` handles exceptions globally
- [ ] HTTP status codes are correct (400, 404, 409, etc.)
- [ ] Error responses include helpful details

### 9. Testing
- [ ] Unit tests exist for all business logic
- [ ] Integration tests cover all service methods
- [ ] API/controller tests cover all endpoints
- [ ] Repository tests cover all queries
- [ ] Validation tests cover all validation rules
- [ ] State-machine tests cover ALL valid and invalid transitions
- [ ] Negative tests exist for error scenarios
- [ ] Tests are derived from acceptance criteria

### 10. Security and Best Practices
- [ ] No secrets or credentials in code
- [ ] SQL injection prevention (using JPA/HQL properly)
- [ ] Input sanitization for user-provided content
- [ ] Appropriate exception messages (no internal details exposed)

### 11. API Standards Compliance
- [ ] REST conventions followed (naming, methods)
- [ ] JSON format for all requests/responses
- [ ] Consistent error response format
- [ ] HTTP status codes are correct
- [ ] Search/filter conventions are followed
- [ ] Pagination is implemented for collections

### 12. Code Review Red Flags (Automatically Flag)

| Red Flag | Action |
|----------|--------|
| `@Autowired` field injection | MUST explain why not constructor injection |
| Entity returned from controller | MUST be DTO |
| Status updated via PATCH/PUT | MUST use transition endpoint |
| No state validation | MUST add validation |
| Missing exception handler | MUST add handler |
| Hardcoded values | MUST externalize |
| No tests for edge case | MUST add test |

### 13. Specification Review

For each implementation element, verify:

1. **Find the spec section** that requires this functionality
2. **Verify completeness**: Does implementation cover all requirements?
3. **Verify correctness**: Does implementation do exactly what spec says?
4. **Verify edge cases**: Does implementation handle all spec-specified edge cases?
5. **Verify error handling**: Does implementation handle all error cases from spec?

### 14. AI Assumption Checking

Question any AI assumptions:

- [ ] Was a "logical" extension added that wasn't in the spec?
- [ ] Was a "convenient" design chosen over spec requirements?
- [ ] Was something "simplified" that the spec requires to be complex?
- [ ] Was a standard pattern applied that conflicts with project requirements?

### Final Review Statement

**If any defects or spec deviations are found, you MUST list them explicitly.**

If no defects are found, you MUST state: "No defects or spec deviations found."

**Do NOT write:** "Code looks good" or "Looks correct" or "Implementation matches spec."

Instead write: "Code review complete. [Summary of findings, or 'No defects or spec deviations found.']"