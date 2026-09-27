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


## AI Implementation Incident — Prompt Recorder Format

### Context
While creating the prompt-recording mechanism, Kiro initially chose an
incorrect implementation approach for a Kiro skill.

### AI Mistake
Kiro initially attempted to create `prompt-recorder.yaml` and described YAML
as the appropriate format for the Kiro skill.

### Correction
Kiro subsequently recognized that the approach was incorrect for the intended
Kiro functionality, deleted the YAML file, and changed the implementation to
a Kiro hook under `.kiro/hooks/`.

### Evidence
The original Kiro conversation shows the initial YAML creation, subsequent
recognition of the incorrect approach, deletion of the YAML file, and creation
of `prompt-recorder.json` and `record-prompt.sh`.


## AI Implementation Incident — External Dependency

### Context
Kiro created the prompt-recording shell script and attempted to test it.

### AI Mistake
The initial implementation of `record-prompt.sh` depended on the `jq`
command without first verifying that `jq` was available in the environment.

### Observed Failure
The first test produced:

`jq: command not found`

### Correction
Kiro modified the script to use an approach that did not require the
missing external dependency and subsequently verified that the simplified
script could record a test prompt.

### Evidence
The original Kiro conversation contains the terminal error and the subsequent
script modification.

## Prompt History Incident — Test Records

### Context
The prompt-recording mechanism was tested during setup.

### Event
Test prompt records were temporarily created under
`.specstory/history/`.

### Action Taken
The temporary test records were removed after verification so that test
input would not be represented as genuine project prompt history.

### Evidence
The Kiro conversation shows creation of the test record, inspection of the
recorded file, and subsequent removal.

