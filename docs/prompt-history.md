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

## AI Implementation Incident — Mapper Dependency

### Context
During implementation of Tasks 2.4 and 2.5, Kiro created the repository
interfaces and then created TicketMapper and CommentMapper.

### AI Mistake
The mapper implementations referenced DTO classes that are scheduled for
later Tasks 4.1 and 4.2. Therefore, the mapper code could not compile at
that stage.

### Human/Engineering Validation
The implementation was reviewed against IMPLEMENTATION-PLAN.md and the
specification dependency chain. The mapper/DTO dependency was identified
as inconsistent with the current implementation order.

### Correction
The prematurely created mapper files were removed. Mapper implementation
will be created when the required DTOs exist, rather than accepting
non-compiling code.

### Evidence
The original Kiro conversation contains the mapper creation, review,
identification of the dependency problem, and subsequent removal.

## AI Implementation Incident — Invalid Entity Imports

### Context
During implementation of the Ticket and Comment entities, Kiro reported that
the entities matched the specification and that diagnostics showed no errors.

### AI Mistake
The generated entity code contained invalid imports/references:

- `jakarta.validation.constraintsNotBlank` was used instead of
  `jakarta.validation.constraints.NotBlank`.
- `OnDelete` and `OnDeleteAction` were incorrectly referenced from
  `jakarta.persistence`. These annotations are provided by Hibernate.

Kiro's initial verification did not detect these compilation problems.

### Observed Failure
A subsequent Java 21 Gradle build failed during `compileJava` with 9
compilation errors, including:

- `cannot find symbol: class constraintsNotBlank`
- `cannot find symbol: class OnDelete`
- `cannot find symbol: class OnDeleteAction`

### Human/Engineering Validation
The project was verified using the actual Java 21 build rather than relying
only on Kiro's diagnostics or summary.

The build output demonstrated that the generated entity code did not compile.

### Correction
The incorrect validation imports were corrected to:

`jakarta.validation.constraints.NotBlank`

The Hibernate delete-cascade annotations were corrected to use:

`org.hibernate.annotations.OnDelete`

and:

`org.hibernate.annotations.OnDeleteAction`

No entity fields or business behavior were changed as part of this correction.

### Evidence
The original Kiro conversation and Gradle build output contain the incorrect
code, the compilation failure, and the subsequent correction.


