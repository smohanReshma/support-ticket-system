## AI Review Incident — State Machine

### Context
During review of `spec/requirements.md`, Kiro incorrectly identified some valid
state transitions as invalid while reviewing the SM-007 invalid-transition table.

### AI Mistake
Kiro initially treated valid transitions such as:

- OPEN -> CANCELLED
- IN_PROGRESS -> CANCELLED

as invalid in its review.

It subsequently detected the inconsistency itself after additional review
and corrected the table.

### Human/Engineering Validation
The state machine was checked against the assignment requirements and the
correct valid transitions were confirmed to be:

- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

All other transitions are invalid.

### Action Taken
The incorrect AI-generated interpretation was not accepted blindly. The
requirements specification was corrected and re-reviewed before proceeding
to the next specification phase.

### Evidence
The original Kiro conversation contains the erroneous review and subsequent
corrections.
