# Specification Review Command

Review the current spec file for completeness and correctness.

## Reviewer Mandate

**You must identify all issues. Do not approve a spec that needs clarification.**

## Specification Review Checklist

### 1. Ambiguity Detection

Identify any vague or ambiguous statements:

- [ ] Are all requirements unambiguous?
- [ ] Are all terms clearly defined?
- [ ] Are edge cases explicitly defined?
- [ ] Are success/failure conditions clear?
- [ ] Are time constraints specified (if applicable)?
- [ ] Are data format requirements explicit?

**Action if found**: Flag all ambiguous statements and request clarification.

### 2. Contradiction Detection

Check for internal inconsistencies:

- [ ] Are requirements contradictory?
- [ ] Do different sections contradict each other?
- [ ] Are API requirements consistent with data model?
- [ ] Are state machine rules consistent with API design?

**Action if found**: List all contradictions and recommend resolution.

### 3. Acceptance Criteria

Check for complete acceptance criteria:

- [ ] Does each feature have acceptance criteria?
- [ ] Are acceptance criteria testable?
- [ ] Are acceptance criteria specific and measurable?
- [ ] Are acceptance criteria derived from requirements?

**Action if found**: Add missing acceptance criteria for any feature.

### 4. Edge Cases

Check for missing edge cases:

- [ ] What happens with empty input?
- [ ] What happens with very large input?
- [ ] What happens with invalid data?
- [ ] What happens with concurrent access?
- [ ] What happens with missing dependencies?
- [ ] What happens with timeouts?
- [ ] What happens with data that doesn't exist?

**Action if found**: Add missing edge cases to spec.

### 5. State Machine Problems

For state machine requirements:

- [ ] Is the initial state defined?
- [ ] Are all valid transitions documented?
- [ ] Are all invalid transitions documented (or implied)?
- [ ] Is the final state defined?
- [ ] Are state transition requirements consistent across all APIs?
- [ ] Does the state machine handle all business scenarios?
- [ ] Are there unreachable states?

**Action if found**: Fix state machine definition and list all issues.

### 6. API Inconsistencies

Check API design consistency:

- [ ] Are endpoints named consistently?
- [ ] Are HTTP methods used consistently?
- [ ] Are error responses consistent?
- [ ] Are request/response formats consistent?
- [ ] Are validation rules consistent?

**Action if found**: Fix inconsistencies and list all issues.

### 7. Data Model Problems

Check data model completeness:

- [ ] Are all entities defined?
- [ ] Are all attributes defined with types?
- [ ] Are relationships between entities clear?
- [ ] Are constraints specified (not null, unique, etc.)?
- [ ] Are indexes defined for performance?
- [ ] Are validations on data model specified?
- [ ] Is data model normalized appropriately?

**Action if found**: Fix data model issues and list all problems.

### 8. Test Scenarios

Check for missing test scenarios:

- [ ] Are all happy paths covered?
- [ ] Are all error paths covered?
- [ ] Are all edge cases tested?
- [ ] Are all state transitions tested?
- [ ] Are negative tests included?
- [ ] Are performance tests included (if needed)?

**Action if found**: Add missing test scenarios.

### 9. Validation Requirements

Check validation completeness:

- [ ] Are all input validations specified?
- [ ] Are all business rule validations specified?
- [ ] Are validation error messages specified?
- [ ] Are validation rules consistent across APIs?

**Action if found**: Add missing validation rules.

### 10. Security Considerations

Check for security gaps:

- [ ] Are authentication requirements specified?
- [ ] Are authorization requirements specified?
- [ ] Are sensitive data fields protected?
- [ ] Are input validation requirements specified for security?
- [ ] Are rate limiting requirements specified?

**Action if found**: Add missing security requirements.

### 11. Performance Considerations

Check for performance gaps:

- [ ] Are pagination requirements specified?
- [ ] Are query optimization requirements specified?
- [ ] Are caching requirements specified?

**Action if found**: Add missing performance requirements.

### 12. Error Handling

Check error handling completeness:

- [ ] Are all error scenarios documented?
- [ ] Are error response formats specified?
- [ ] Are error codes specified?
- [ ] Are user-friendly error messages specified?

**Action if found**: Add missing error handling requirements.

### Final Review Statement

**If any issues are found, you MUST list them explicitly with specific section references.**

If no issues are found, you MUST state: "No issues found. Specification is complete and ready for implementation."

**Do NOT write:** "Spec looks good" or "Spec seems okay" or "Specification is correct."

Instead write: "Specification review complete. [Summary of findings, or 'No issues found. Specification is complete and ready for implementation.']"

### Specification Quality Gates

A specification passes review only if:

1. **No ambiguity** - Every requirement is clear and unambiguous
2. **No contradictions** - All requirements are internally consistent
3. **Complete acceptance criteria** - Every feature has testable acceptance criteria
4. **All edge cases covered** - Edge cases are explicitly defined
5. **State machine is complete** - All states and transitions are documented
6. **Test scenarios complete** - All test scenarios are identified
7. **Validation complete** - All validation rules are specified
8. **Error handling complete** - All error scenarios are documented