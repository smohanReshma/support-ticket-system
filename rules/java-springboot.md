# Java Spring Boot Development Rules

## Project Configuration
- Java version: 21
- Spring Boot version: 4.1.1
- Gradle version: 8.10 (use Gradle wrapper)

## Project Structure

```
org.c2.supportticketsystem
├── config/              # Configuration classes (Security, WebMvc, etc.)
├── controller/          # REST API endpoints (thin layer, delegates to service)
├── service/             # Business logic layer (contains state machine logic)
├── repository/          # Data access layer (JPA repositories)
├── model/               # Domain models (entities)
├── dto/                 # Data Transfer Objects (never expose entities directly)
├── exception/           # Custom exceptions
├── validator/           # Custom validation annotations
└── mapper/              # DTO-Entity mapping logic
```

## Layered Architecture

```
                    ┌─────────────────┐
                    │   Controller    │  REST API layer (HTTP)
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │    Service      │  Business logic & state machine
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Repository     │  Data access layer
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │    Database     │  Persistence
                    └─────────────────┘
```

## Coding Standards

### Dependency Injection
- **Always use constructor injection** instead of field injection
- Field injection prevents unit testing without Spring context
- Constructor injection makes dependencies explicit

```java
// Good
@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
}

// Bad
@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
}
```

### Layer Responsibilities
- **Controller**: HTTP request/response handling, validation, no business logic
- **Service**: Business logic, state machine enforcement, transaction boundaries
- **Repository**: Data access only, no business logic

### DTO Pattern
- **Never expose entities directly in API responses**
- Create separate DTO classes for API contracts
- Entities are persistence concerns
- DTOs are API contracts
- Use mapping libraries (MapStruct) or manual mapping

### Validation
- Use Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.)
- Validate at controller level before service call
- Create custom validators for complex validation rules
- Return `400 Bad Request` with detailed error messages for validation failures

### Exception Handling
- Create custom exceptions for domain-specific errors
- Use `@ControllerAdvice` for global exception handling
- Return meaningful error responses with HTTP status codes
- Log exceptions appropriately

```java
@ExceptionHandler(TicketNotFoundException.class)
@ResponseStatus(NOT_FOUND)
public ErrorDetail handleTicketNotFound(TicketNotFoundException ex) {
    return new ErrorDetail(ex.getMessage());
}
```

### Transaction Boundaries
- Use `@Transactional` on service methods that modify data
- Read-only operations should use `@Transactional(readOnly = true)`
- Transaction boundaries should be at service layer, not repository
- Keep transactions as short as possible

### Configuration and Secrets Hygiene
- **Never commit secrets** (API keys, passwords, tokens)
- Use `application.properties` for non-sensitive configuration
- Use environment variables for sensitive configuration
- Use Spring Cloud Config or similar for centralized configuration
- Add `application-local.properties` to `.gitignore`
- Use `@Value` with defaults for optional configuration
- Use `@ConfigurationProperties` for type-safe configuration groups

```properties
# Good - externalized configuration
ticket.state-transition.enforce=true

# Bad - hardcoded values
# ticket.state-transition.enforce=true  # should be in external config
```

### State Machine Enforcement
- **State transitions MUST be enforced in the backend**
- Frontend cannot be trusted for state transitions
- Implement state machine logic in service layer
- Reject invalid transitions with `409 Conflict`
- Never allow state changes that bypass the state machine
- Validate state transitions before persisting
- Include current state in all responses

```java
public void updateStatus(Long ticketId, Status newStatus) {
    Ticket ticket = repository.findById(ticketId)
        .orElseThrow(() -> new TicketNotFoundException(ticketId));
    
    if (!isValidTransition(ticket.getStatus(), newStatus)) {
        throw new InvalidStateTransitionException(
            ticket.getStatus(), newStatus);
    }
    
    ticket.setStatus(newStatus);
    repository.save(ticket);
}
```