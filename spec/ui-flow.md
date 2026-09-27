# Support Ticket Management System - UI Flow

## 1. Overview

This document defines the user interface flows and error handling requirements for the Support Ticket Management System. The UI is implemented with React/Next.js and follows the requirements specified in `FR-009: UI Error Display`.

## 2. UI Components and Screens

### 2.1 Application Layout

```
AppLayout
├── Header
│   └── Navigation links (Home, Create Ticket)
├── Main Content Area
│   ├── TicketListView (home page)
│   ├── TicketCreateView (create new ticket)
│   ├── TicketDetailView (ticket details and comments)
│   └── ErrorBoundary (global error handling)
└── Footer (optional)
```

### 2.2 Global Components

| Component | Purpose |
|-----------|---------|
| AppLayout | Main layout wrapper with navigation |
| ErrorDisplay | Generic error display component |
| LoadingSpinner | Loading indicator |
| ViewStateTransition | State transition action component |
| ToastNotification | Success/error notifications |

## 3. User Flows

### 3.1 Ticket List View (Home Page)

**Route:** `/`

**Purpose:** Display all tickets in a paginated list

**UI Elements:**
- Ticket cards with summary information
- Pagination controls
- Filter by status dropdown
- Search input
- "Create New Ticket" button

**Flow:**
```
1. User navigates to /
2. System fetches tickets with pagination
3. Display tickets in cards
4. User can:
   - Click on ticket to view details
   - Use search to filter tickets
   - Use status filter to filter tickets
   - Click "Create New Ticket" button
```

**Data Displayed per Ticket:**
- Title (truncated if needed)
- Status badge (color-coded)
- Priority badge (color-coded)
- Created date
- Assignee (if assigned)

---

### 3.2 Create Ticket View

**Route:** `/tickets/new`

**Purpose:** Create a new support ticket

**UI Elements:**
- Title input field
- Description textarea
- Priority radio buttons/select
- Assignee input field (optional)
- Cancel button
- Submit button

**Flow:**
```
1. User clicks "Create New Ticket"
2. User fills in ticket details
3. User submits form
4. System validates input
5. On success: redirect to ticket details
6. On validation error: display errors inline
7. On network error: display error banner
```

**Validation Errors Displayed:**
- Title: "must not be blank", "must be no more than 255 characters"
- Description: "must not be blank", "must be no more than 10000 characters"
- Priority: "must not be null", "must be one of: LOW, MEDIUM, HIGH, URGENT"
- Assignee: "must be no more than 255 characters"

---

### 3.3 Ticket Detail View

**Route:** `/tickets/{id}`

**Purpose:** Display ticket details and associated comments

**UI Elements:**
- Ticket information card
- Status badge with transition controls
- Comments section
- Add comment form
- Edit ticket button
- Back to list button

**Flow:**
```
1. User clicks on ticket from list
2. System fetches ticket details with comments
3. Display ticket information
4. Display comments in chronological order
5. User can:
   - Transition ticket status (if valid)
   - Add comment
   - Edit ticket details
   - Navigate back to list
```

**Data Displayed:**
- All ticket fields
- Comments (sorted by createdAt ascending)
- Timestamps (createdAt, updatedAt)

---

### 3.4 Edit Ticket View

**Route:** `/tickets/{id}/edit`

**Purpose:** Update ticket fields (except status)

**UI Elements:**
- Title input field
- Description textarea
- Priority radio buttons/select
- Assignee input field
- Cancel button
- Save button

**Flow:**
```
1. User clicks "Edit" from ticket detail view
2. Form is pre-filled with current values
3. User modifies fields
4. User submits form
5. System validates input
6. On success: redirect to ticket details
7. On validation error: display errors inline
8. On network error: display error banner
```

---

## 4. Error Handling Requirements (FR-009)

### 4.1 General Requirements

The UI shall:

1. Present validation errors from the backend to the user in a clear and accessible manner
2. Present state transition errors (409 Conflict) to the user with information about valid next states
3. Present resource not found errors (404) to the user when applicable
4. Display error messages prominently and remain visible until the user dismisses them or the underlying issue is resolved
5. Present error messages in a way that does not block user interaction with other parts of the application

### 4.2 Validation Error Display (FR-009.1)

**Trigger:** Backend returns HTTP 400 Bad Request with violations array

**UI Behavior:**
```typescript
// Example: Displaying validation errors
const ValidationErrors = ({ violations }: { violations: Violation[] }) => (
  <div className="error-banner error-validation">
    <h3>Validation errors:</h3>
    <ul>
      {violations.map((v, i) => (
        <li key={i}>
          <strong>{v.field}:</strong> {v.message}
        </li>
      ))}
    </ul>
  </div>
);
```

**Inline Field Errors:**
- Display error message below the affected input field
- Add visual indicator (red border, error icon)
- Keep error message visible until input is corrected

**Banner Error:**
- Display at top of form or page
- Summarize all validation errors
- Allow user to dismiss if they want to address them one by one

### 4.3 State Transition Error Display (FR-009.2)

**Trigger:** Backend returns HTTP 409 Conflict with state transition error

**Error Response Example:**
```json
{
  "currentStatus": "OPEN",
  "requestedStatus": "RESOLVED",
  "validTransitions": ["IN_PROGRESS", "CANCELLED"]
}
```

**UI Behavior:**
```typescript
// Example: Displaying state transition errors
const StateTransitionError = ({ error }: { error: TransitionError }) => (
  <div className="error-banner error-transition">
    <h3>Cannot transition to {error.requestedStatus}</h3>
    <p>Current state: {error.currentStatus}</p>
    <p>Valid next states: {error.validTransitions.join(', ')}</p>
    <button onClick={() => handleSelectValidTransition(error.validTransitions)}>
      Select valid transition
    </button>
  </div>
);
```

**UI Requirements:**
- Display current state and requested state clearly
- Show list of valid transitions for current state
- Provide a way for user to select a valid transition
- Do not block other UI interactions
- Keep the error visible until user takes action to dismiss

### 4.4 Resource Not Found Error Display (FR-009.3)

**Trigger:** Backend returns HTTP 404 Not Found

**UI Behavior:**
```typescript
// Example: Displaying 404 errors
const NotFoundError = ({ resourceType, resourceId }: { resourceType: string, resourceId: string }) => (
  <div className="error-banner error-not-found">
    <h3>Resource not found</h3>
    <p>The requested {resourceType} (ID: {resourceId}) could not be found.</p>
    <button onClick={() => navigate(-1)}>Go back</button>
    <button onClick={() => navigate('/')}>Go to home</button>
  </div>
);
```

**UI Requirements:**
- Clearly indicate the resource type and ID
- Provide navigation options (go back, go to home)
- Keep error visible until user takes action

### 4.5 Network/Error Response Display (FR-009.4)

**Trigger:** Network error or HTTP 500 Internal Server Error

**UI Behavior:**
```typescript
// Example: Displaying network/server errors
const NetworkError = ({ message }: { message: string }) => (
  <div className="error-banner error-network">
    <h3>An error occurred</h3>
    <p>{message}</p>
    <button onClick={retryAction}>Try again</button>
    <button onClick={dismissAction}>Dismiss</button>
  </div>
);
```

**UI Requirements:**
- Distinguish between user error (400, 404) and system error (500)
- Provide retry option for network errors
- Keep error visible until user takes action

---

## 5. Error Component Specifications

### 5.1 ErrorBanner Component

```typescript
interface ErrorBannerProps {
  type: 'validation' | 'transition' | 'not-found' | 'network';
  title: string;
  message: string;
  details?: string;
  onDismiss?: () => void;
  onRetry?: () => void;
}

export const ErrorBanner = ({ type, title, message, details, onDismiss, onRetry }: ErrorBannerProps) => (
  <div className={`error-banner error-${type}`}>
    <div className="error-header">
      <h3>{title}</h3>
      {onDismiss && (
        <button onClick={onDismiss} aria-label="Dismiss error">
          ×
        </button>
      )}
    </div>
    <p>{message}</p>
    {details && <p className="error-details">{details}</p>}
    {onRetry && <button onClick={onRetry}>Try again</button>}
  </div>
);
```

### 5.2 InlineFieldError Component

```typescript
interface InlineFieldErrorProps {
  fieldName: string;
  message: string;
}

export const InlineFieldError = ({ fieldName, message }: InlineFieldErrorProps) => (
  <div className="field-error">
    <span>{message}</span>
  </div>
);
```

---

## 6. State Transition UI Flow

### 6.1 Transition Control Display

```typescript
const StateTransitionControl = ({ 
  currentStatus, 
  onTransition 
}: { 
  currentStatus: Status, 
  onTransition: (target: Status) => void 
}) => {
  const validTransitions = getValidTransitions(currentStatus);
  
  return (
    <div className="state-transition">
      <span>Current status: {currentStatus}</span>
      <div className="transition-actions">
        {validTransitions.length === 0 ? (
          <span>No transitions available</span>
        ) : (
          <select 
            onChange={(e) => onTransition(e.target.value as Status)}
            value=""
          >
            <option value="">Select a transition...</option>
            {validTransitions.map(status => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>
        )}
      </div>
    </div>
  );
};
```

### 6.2 Transition Error Handling

```typescript
const TransitionWithErrorHandling = ({ 
  currentStatus, 
  onTransition 
}: { 
  currentStatus: Status, 
  onTransition: (target: Status) => void 
}) => {
  const [transitionError, setTransitionError] = useState<TransitionError | null>(null);
  
  const handleTransition = async (targetStatus: Status) => {
    try {
      await api.transitionStatus(ticketId, targetStatus);
      // Success: clear error, update state
      setTransitionError(null);
      // ... update UI
    } catch (error) {
      if (error instanceof TransitionError) {
        setTransitionError(error);
      } else {
        // Handle other errors (network, etc.)
      }
    }
  };
  
  return (
    <>
      <StateTransitionControl 
        currentStatus={currentStatus}
        onTransition={handleTransition}
      />
      {transitionError && (
        <StateTransitionError error={transitionError} />
      )}
    </>
  );
};
```

---

## 7. Accessibility Requirements

### 7.1 Error Announcements

- Use ARIA live regions for dynamic error messages
- Screen readers should announce new errors immediately

### 7.2 Error Focus

- Focus should be moved to error banner or first invalid field
- Use `aria-describedby` to associate errors with fields

### 7.3 Contrast and Visibility

- Error messages must meet WCAG contrast requirements
- Use color + text for error indication (not color alone)

---

## 8. Example User Journey

### 8.1 Create Ticket Journey

1. User navigates to `/`
2. User clicks "Create New Ticket" button
3. User sees form with validation rules
4. User fills in ticket details
5. User submits form
6. If valid: user redirected to ticket details
7. If invalid: validation errors displayed inline and in banner

### 8.2 State Transition Journey

1. User views ticket details (status: OPEN)
2. User sees "Transition" dropdown with options: IN_PROGRESS, CANCELLED
3. User selects "RESOLVED" (invalid transition)
4. User sees error banner:
   - "Cannot transition to RESOLVED"
   - "Current state: OPEN"
   - "Valid next states: IN_PROGRESS, CANCELLED"
5. User selects "IN_PROGRESS" from dropdown
6. Success: status updates, error banner dismissed

### 8.3 Error Display Journey

1. User encounters any error (validation, transition, network)
2. Error banner appears prominently
3. User can read error message and take action
4. User can dismiss error or retry action
5. Error does not block other UI interactions
