# Support Ticket Management System - Architecture

## 1. Overview

This document defines the system architecture for the Support Ticket Management System, including technology stack, layered design, and deployment considerations.

## 2. Technology Stack

### Backend
| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 4.1.1 |
| Build Tool | Gradle | 9.7.1 |
| Database | PostgreSQL | Latest stable |
| In-Memory DB | H2 | Latest stable (dev/testing) |
| JPA Provider | Hibernate | Built-in |
| Validation | Bean Validation (Jakarta) | 3.0 |

### Frontend
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | React | Latest LTS |
| Routing | Next.js | Latest LTS |
| State Management | React Context or Zustand | Latest stable |
| Styling | CSS Modules or Tailwind CSS | Latest stable |
| HTTP Client | axios or fetch API | Latest stable |

## 3. Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend Layer                        │
│  (React/Next.js - Browser)                                  │
│  - Ticket List View                                         │
│  - Ticket Detail View                                       │
│  - Create Ticket Form                                       │
│  - Error Display Components                                 │
└─────────────────────────────────────────────────────────────┘
                            │ HTTPS/REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      API/Controller Layer                    │
│  (org.c2.supportticketsystem.controller)                    │
│  - REST endpoint handlers                                   │
│  - Request validation (@Valid)                              │
│  - Response DTO construction                                │
│  - No business logic                                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
│  (org.c2.supportticketsystem.service)                       │
│  - Business logic                                           │
│  - State machine enforcement                                │
│  - Transaction boundaries (@Transactional)                  │
│  - Validation orchestration                                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│  (org.c2.supportticketsystem.repository)                    │
│  - JPA repositories                                         │
│  - Custom query methods                                     │
│  - Data access only, no business logic                      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  (PostgreSQL/H2 Database)                                   │
│  - Persistent storage                                       │
│  - Constraints and indexes                                  │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Responsibility | What NOT to do |
|-------|---------------|----------------|
| Controller | HTTP request/response, validation | Business logic, data access |
| Service | Business rules, state machine, transactions | HTTP details, direct database access |
| Repository | Data persistence | Business logic, state machine |
| Entity | Data representation | Business logic, API contracts |

## 4. Package Structure

```
org.c2.supportticketsystem
├── config/                           # Spring configuration
│   ├── ApplicationConfig.java
│   └── JacksonConfig.java
├── controller/                       # REST API endpoints
│   ├── TicketController.java
│   └── CommentController.java
├── service/                          # Business logic layer
│   ├── TicketService.java
│   ├── CommentService.java
│   └── StateMachineService.java
├── repository/                       # Data access layer
│   ├── TicketRepository.java
│   └── CommentRepository.java
├── model/                            # Domain entities
│   ├── Ticket.java
│   ├── Comment.java
│   └── enums/
│       ├── Status.java
│       └── Priority.java
├── dto/                              # Data Transfer Objects
│   ├── request/
│   │   ├── CreateTicketRequest.java
│   │   ├── UpdateTicketRequest.java
│   │   ├── AddCommentRequest.java
│   │   └── TransitionRequest.java
│   └── response/
│       ├── TicketResponse.java
│       ├── CommentResponse.java
│       └── TicketListResponse.java
├── exception/                        # Custom exceptions
│   ├── TicketNotFoundException.java
│   ├── InvalidStateTransitionException.java
│   └── GlobalExceptionHandler.java
└── mapper/                           # DTO-Entity mapping
    ├── TicketMapper.java
    └── CommentMapper.java
```

## 5. State Machine Location

The state machine logic is implemented in `StateTransitionService.java` within the service layer. The service:

1. Receives state transition requests from controllers
2. Validates the transition against the state machine rules
3. Updates the ticket status
4. Persists changes with transactional boundaries

## 6. Database Configuration

### Development/Testing
- **H2 Database** (in-memory or file-based)
- Auto-create schema: `spring.jpa.hibernate.ddl-auto=update`
- Console enabled: `spring.h2.console.enabled=true`

### Production
- **PostgreSQL** database
- Auto-create schema: `spring.jpa.hibernate.ddl-auto=none` (use Flyway/Liquibase for migrations)
- Connection pool: HikariCP (default)

### Configuration Properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/tickets
spring.datasource.username=tickets_user
spring.datasource.password=${DB_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# H2 (development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 7. API Contract

All API endpoints follow the REST standards defined in `rules/api-standards.md`:

- Versioned endpoints: `/api/v1/*`
- JSON request/response format
- camelCase field naming
- Standard HTTP status codes
- Pagination for collection endpoints
- Validation with detailed error responses

## 8. Frontend Architecture

### Next.js Structure
```
src/
├── app/
│   ├── layout.tsx                    # Root layout with error boundaries
│   ├── page.tsx                      # Ticket list view
│   ├── tickets/
│   │   ├── page.tsx                  # New ticket form
│   │   └── [id]/
│   │       ├── page.tsx              # Ticket detail view
│   │       └── edit/page.tsx         # Edit ticket view
├── components/
│   ├── ui/                           # Reusable UI components
│   │   ├── ErrorDisplay.tsx
│   │   ├── TicketCard.tsx
│   │   ├── TicketForm.tsx
│   │   └── StateTransition.tsx
│   └── layouts/
│       └── AppLayout.tsx
├── lib/
│   ├── api/                          # API client utilities
│   │   ├── client.ts
│   │   └── endpoints.ts
│   └── utils/                        # Helper functions
│       └── format.ts
└── types/
    └── ticket.ts
```

### Frontend State Management
- Local state for form inputs
- React Context for global state (ticket list, current ticket)
- Server state via React Query for API data fetching

### Error Handling
- Global error boundary component
- Per-component error display
- Toast notifications for success messages
- Inline validation error messages

## 9. Deployment Considerations

### Development
- Hot reload enabled
- H2 database for quick iteration
- Local development server

### Production
- Build: `npm run build` (frontend), `./gradlew build` (backend)
- Static files served by web server (Nginx, Apache)
- Backend as JAR with embedded Tomcat
- Environment variables for configuration
- Health check endpoint: `/actuator/health`

### Environment Variables Required
```bash
# Backend
DB_PASSWORD=secure_password
DB_HOST=production-db.example.com
SPRING_PROFILES_ACTIVE=prod

# Frontend
NEXT_PUBLIC_API_URL=https://api.example.com
```

## 10. Security Considerations

- Parameterized queries for SQL injection prevention
- Input validation at controller and service layers
- No hardcoded secrets (all externalized)
- CORS configuration for frontend-backend communication
- HTTPS in production
- Secret management via environment variables or vault

## 11. Performance Requirements

- API response time: < 2 seconds under normal load
- Database queries optimized with proper indexes
- Connection pooling enabled
- Caching considered for frequently accessed data (future enhancement)
