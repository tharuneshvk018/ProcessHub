# PROCESSHUB — PRODUCT VISION, ARCHITECTURE AND IMPLEMENTATION DIRECTIVE

## 1. Product identity and mission

Product name: ProcessHub

Product category: Intelligent problem reporting, workflow coordination, approval, and resolution platform.

Build ProcessHub as an independent software product intended for real organizations and their users.

ProcessHub must have a consistent product identity, professional user experience, maintainable architecture, secure implementation, and a credible path to public release.

The product must not be presented anywhere in its user-facing interface, marketing copy, README, documentation, source-code comments, demo content, or onboarding as a college project, academic project, student assignment, university submission, or educational demonstration.

Do not introduce academic framing into the product. Use professional product language appropriate for real customers, administrators, employees, service teams, and end users.

The product name must remain ProcessHub throughout the application, documentation, configuration, and branding unless a technical identifier cannot reasonably use the name.

## 2. Core product problem

Organizations receive problems and service requests through scattered channels such as messages, emails, verbal reports, forms, and phone calls.

This creates recurring difficulties:
- Reports reach the wrong department.
- Nobody clearly owns the next action.
- Approvals and repairs become disconnected.
- Users cannot tell why a request is delayed.
- Repeated problems are reported without useful historical context.
- Work may be marked complete without adequate verification.
- Managers lack reliable information about recurring failures and resolution performance.

ProcessHub should provide one structured path from initial reporting to verified resolution.

A user reports a problem. ProcessHub identifies the applicable workflow, routes the work to authorized people, coordinates required decisions, tracks progress, and records the outcome.

Do not reduce ProcessHub to a complaint submission form, a generic CRUD dashboard, a social feed, or a basic ticket status tracker.

## 3. Product scope and initial market

The initial product configuration should support a college or campus organization as the first deployment environment.

However, build the underlying architecture so that organization-specific departments, roles, categories, approval rules, locations, and workflows are configurable rather than permanently hardcoded.

The initial deployment should include realistic examples of:
- Facilities and maintenance
- Plumbing
- Electrical maintenance
- IT and network support
- Laboratory support
- Administration
- Finance and expenditure approvals

Do not implement separate applications for each department.

Use a shared product and a common workflow system, with configurable organization-specific rules.

Keep the initial scope manageable. Build a complete and reliable core before adding additional domains or advanced features.

## 4. Product research and differentiation

Before finalizing the feature set, examine relevant existing products and their documented capabilities.

Investigate representative products such as:
- Jira Service Management for request intake, queues, assignment, and service workflows.
- Camunda for workflow execution, human tasks, and conditional process steps.
- GLPI for support requests, asset-related operations, and approval workflows.
- Relevant facilities and maintenance-management products for equipment records, work orders, and recurring failures.

Use official documentation, product pages, and credible technical sources. When possible, inspect real product workflows rather than relying only on marketing descriptions.

Create an internal comparison of:
- Core capabilities already commonly available.
- Typical user journeys and workflow patterns.
- Limitations documented by product providers or reported by credible users.
- Problems that remain poorly addressed for the intended target customers.
- Opportunities that can be implemented realistically within ProcessHub.

Distinguish verified findings from assumptions. Do not invent competitor weaknesses or claim a feature is unique without sufficient evidence.

Use the research to inform product decisions and implementation priorities. The resulting application must not simply reproduce an existing product's feature list or interface.

Translate justified product decisions into useful functionality, not merely renamed features.

Do not turn the product interface, documentation, or ordinary development reports into a discussion of novelty, originality, or competitor analysis. Those are internal planning activities, not end-user features.

Do not add promotional claims such as "first of its kind", "world's first", "revolutionary", or "unique AI-powered solution" without independently substantiated evidence.

The objective is a meaningfully differentiated product, not a product that merely claims to be different.

## 5. Product differentiation requirements

Investigate and prioritize improvements that address actual workflow failures.

Evaluate the following opportunities and implement only those justified by the research and achievable within the agreed scope.

### A. Context-aware issue routing

Routing should consider more than the category name.

Where configured, consider:
- Issue category and subtype.
- Location and affected asset.
- Severity and operational impact.
- Responsible department.
- Required authorization.
- Availability of eligible assignees.
- Existing open issues affecting the same location or asset.

If the system cannot safely determine a destination, create a visible exception for an authorized administrator rather than silently assigning the issue incorrectly.

### B. Explainable workflow progress

Users and authorized staff should be able to understand:
- The current stage of an issue.
- Which action is required next.
- Which role or team is responsible.
- Whether an approval or additional information is blocking progress.
- Why the issue was routed to a particular team, when appropriate.
- What must happen before closure.

Avoid ambiguous status labels that leave users unable to determine what is happening.

### C. Recurring-issue intelligence

Maintain useful issue and asset history.

Where sufficient data exists, identify repeated reports involving the same asset, location, category, or similar symptoms.

Provide useful context to authorized staff, including previous resolutions and the frequency of recurrence.

Do not claim that similarity detection, root-cause analysis, or prediction is accurate without implementing and testing the relevant logic.

### D. Resolution accountability

Preserve the relationship between:
- The original report.
- Review and approval decisions.
- Work assignments.
- Diagnosis and repair actions.
- Financial authorization, where required.
- Completion evidence.
- Verification and closure.

Do not allow a workflow to appear complete while mandatory steps remain outstanding.

### E. Workflow exception handling

Design for practical failure scenarios:
- No reviewer is configured.
- An assignee is unavailable.
- A deadline is missed.
- An approval is rejected.
- Additional information is required.
- Expenditure is not authorized.
- A repair fails verification.
- An issue recurs after closure.

Each scenario must have a defined, auditable next action.

These are product capabilities to evaluate and implement where justified. Do not add artificial complexity merely to make the application appear advanced.

## 6. Authentication, organizations, and authorization

Provide one shared authentication system.

Different users must not need different login pages based on their role.

Each account should be associated with one or more authorized organization memberships. Each membership should define the user's role assignments and permitted departments.

Initial roles:
- Reporter
- Department reviewer or supervisor
- Technician or resolution team member
- Finance approver
- Organization administrator

Implement role-based permissions and department-level access restrictions.

Support configurable role definitions and permission assignments where practical. Do not assume every organization uses identical job titles or organizational structures.

An organization administrator must be able to configure departments, users, memberships, categories, workflow definitions, approval rules, and escalation policies.

Public registration must never automatically grant administrative, financial, or approval privileges.

Use secure password hashing, Spring Security, validated authentication, appropriate session or token handling, and backend-enforced authorization.

Protect organization data from cross-organization access. A user must not obtain access by changing an identifier in a URL or API request.

Implement account recovery and secure invitation or approval flows according to the chosen authentication architecture.

## 7. Problem reporting and classification

Provide a professional reporting experience.

Capture:
- Issue category and subtype.
- Title and description.
- Location and relevant building or room.
- Affected asset, when applicable.
- Urgency and impact.
- Optional supporting images or documents.
- Submission timestamp and reporter identity.

Use category-specific fields where they materially improve routing and diagnosis.

Examples:
- Water leakage: location, severity, and evidence.
- Laboratory equipment: asset identifier and observed fault.
- Network issue: affected area and connection symptoms.
- Electrical issue: location, observed problem, and relevant safety impact.

Reporters must select the problem and its location, not manually guess which employee should receive it.

Validate inputs, control upload types and sizes, prevent duplicate submissions caused by repeated clicks, and generate a readable reference such as PH-1001.

Do not claim that a report was delivered, accepted, approved, or resolved until the corresponding backend operation succeeds.

## 8. Workflow execution

Build a reliable backend-controlled workflow service.

Workflow rules should be configurable through persisted definitions where appropriate. Do not spread hardcoded routing logic throughout controllers and frontend components.

A workflow instance must track the lifecycle of an individual issue.

Support:
- Submission and initial review.
- Approval, rejection, and requests for information.
- Department routing and work assignment.
- Work-in-progress updates.
- Conditional financial approval.
- Completion evidence.
- Verification.
- Closure and authorized reopening.
- Deadlines and escalation.
- Exception handling.

### Reference workflow: maintenance issue

1. A reporter submits a water-leakage report.
2. ProcessHub creates the issue and its workflow instance.
3. Routing rules identify the authorized Facilities Supervisor.
4. The supervisor reviews the report and decides the next permitted action.
5. The report may be approved, rejected with a reason, or returned for more information.
6. Approved work is assigned to an eligible maintenance team.
7. The technician records diagnosis and repair progress.
8. If additional spending requires authorization, a finance approval task is created.
9. An approved expenditure allows the relevant work to proceed. A rejected expenditure returns the issue to an authorized decision-maker.
10. The technician submits completed work for verification.
11. An authorized person verifies the outcome.
12. ProcessHub closes the issue and retains its history.

Simple repairs must not be delayed by unnecessary finance approvals.

Urgent safety-related issues must follow an explicit escalation policy and must not remain silently blocked in routine queues.

Implement allowed status transitions, required permissions, mandatory decision reasons, and workflow conditions on the backend.

## 9. Notifications and work queues

Provide persistent in-app notifications.

Generate notifications for:
- Successful submission.
- New review tasks.
- Approval or rejection.
- Requests for more information.
- Work assignment and reassignment.
- Technician updates.
- Financial approval requests and decisions.
- Approaching or missed deadlines.
- Escalations.
- Verification requests.
- Closure and reopening.

Every notification must correspond to an actual backend event.

Users must see only information they are authorized to access.

Implement unread counts, timestamps, links to the relevant issue, and persistent read/unread state.

Design work queues around the action required from each role.

Supervisors should see pending reviews, unassigned work, overdue issues, and escalations.

Technicians should see their assigned work, priorities, deadlines, and relevant history.

Finance approvers should see expenditure requests awaiting their decision.

Administrators should see routing exceptions, workflow configuration problems, and organization-level operational metrics.

Do not rely on a shared dashboard with different labels as a substitute for proper role-specific functionality.

## 10. Issue history, audit, and verification

Implement a clear issue timeline.

Record important events:
- Submission.
- Review decisions.
- Assignment changes.
- Status transitions.
- Approval decisions.
- Financial authorization.
- Diagnosis and repair updates.
- Verification.
- Closure and reopening.
- Escalation.

Record the actor, timestamp, relevant task or issue, action, and appropriate details.

Ordinary users must not be able to edit or delete audit records.

Use transactional operations where issue changes, workflow tasks, notifications, and audit events must remain consistent.

Prevent duplicate approvals, unauthorized reassignment, and invalid transitions caused by repeated or concurrent requests.

Define who can verify a resolution. The person performing the repair must not automatically be the only person authorized to verify it when the configured policy requires independent verification.

## 11. Dashboards, analytics, and usability

Build the following functional areas:
- Authentication and account management.
- Reporter dashboard and issue submission.
- My Reports and issue details.
- Supervisor review queue.
- Technician work queue.
- Financial approval queue.
- Notifications.
- Department and user administration.
- Category and workflow configuration.
- Audit history and operational analytics.

Analytics may include:
- Open and overdue issues.
- Time spent in each workflow stage.
- Resolution duration.
- Recurring issues by location or asset.
- Workload by department.
- Issues awaiting approval or verification.

Define metric calculations precisely. Do not show fabricated analytics, fake activity, or hardcoded production statistics.

Use realistic empty states when no data exists.

The interface must be responsive, accessible, consistent, and suitable for professional use. Provide loading states, validation, clear errors, confirmation for destructive actions, and understandable status indicators.

Every visible primary action must work or be clearly identified as unavailable. Do not create decorative buttons that imply functionality that does not exist.

## 12. Technical architecture

Preferred technology stack, subject to the current repository:
- React frontend.
- Java and Spring Boot backend.
- PostgreSQL database.
- Maven.
- Spring Security.
- JUnit 5 and Mockito.
- Database migrations.
- OpenAPI documentation.
- Git and GitHub.

Inspect the existing repository before making architecture changes.

Use clear separation of concerns:
- Presentation layer.
- API and validation layer.
- Application services.
- Workflow and authorization services.
- Persistence layer.
- Notification and audit services.

Use DTOs, input validation, centralized error handling, pagination, appropriate indexes, database constraints, and documented API contracts.

Use environment variables for configuration and provide an example environment file without real credentials.

Do not hardcode secrets, use insecure shortcuts, or expose private attachment URLs.

Use a controlled file-storage implementation with appropriate access checks.

Do not add unnecessary microservices. Prefer a well-structured, maintainable architecture that can be deployed and operated reliably.

## 13. Testing and reliability

Write automated tests for the actual behavior.

Test:
- Authentication and permissions.
- Organization and department isolation.
- Issue creation and validation.
- Category-based routing.
- Approval and rejection.
- Requests for additional information.
- Conditional financial authorization.
- Assignment and reassignment.
- Notification generation.
- Invalid status transitions.
- Audit history.
- Verification and closure.
- Escalation and reopening.
- Duplicate requests and concurrent actions.
- Attachment access controls.
- Failure handling and database persistence.

Use unit and integration tests, and add end-to-end tests for critical user journeys where the current tooling supports them.

Required end-to-end scenarios include:
1. Direct maintenance repair without financial approval.
2. Maintenance repair requiring financial approval.
3. Rejected report with a recorded reason.
4. Request for information and subsequent resubmission.
5. Correct routing between IT and Facilities.
6. Unauthorized approval attempt.
7. Missing reviewer configuration.
8. Work completed but awaiting verification.
9. Missed deadline and escalation.
10. Recurring issue with accessible historical records.

Do not mark a feature complete merely because it compiles or a screen renders.

Run the relevant tests and report actual results.

## 14. Release and operations readiness

Prepare the product for a controlled release through incremental implementation.

Include:
- Reliable setup and deployment instructions.
- Versioned database migrations.
- Secure environment configuration.
- Error logging and operational diagnostics.
- Health checks where appropriate.
- Defined authorization and data-retention behavior.
- Backup and recovery considerations.
- Dependency and security checks.
- Automated test execution.
- Reproducible builds.
- Release notes and versioning.
- Documented known limitations.

Integrate SonarQube, Jenkins, and Docker incrementally.

The CI pipeline should build the application, run tests, perform configured quality checks, and produce a reproducible artifact or container image.

Do not claim that deployment, monitoring, backups, or recovery work unless they have actually been implemented and tested.

Distinguish a locally working build from a release candidate and a verified production deployment.

## 15. Mandatory development discipline

The existing repository is the source of truth.

Before editing:
- Inspect the files and Git status.
- Identify working behavior.
- Identify the root cause of reported defects.
- Determine the smallest safe change.

During implementation:
- Make small, reviewable changes.
- Preserve unrelated working functionality.
- Avoid unnecessary dependencies and rewrites.
- Use reversible database migrations.
- Keep secrets out of version control.
- Test each affected feature.
- Stop and explain any change requiring a major architectural decision.

After each milestone, report:
- Functionality implemented.
- Files changed.
- Database or configuration changes.
- Tests executed and actual results.
- Known defects and risks.
- Recommended next step.

Maintain:
- PROJECT_PROGRESS.md for verified implementation progress.
- ARCHITECTURE.md for actual technical decisions.
- AGENTS.md for persistent repository-specific development rules.
- README.md for product setup and operation.

These documents must describe the actual application, not aspirational features that have not been implemented.

Do not overwrite user changes or replace the entire application to fix a localized issue.

## 16. Implementation stages

Stage 0: Inspect the repository, identify current behavior, and establish a verified baseline.

Stage 1: Stabilize the foundation, database configuration, and authentication.

Stage 2: Implement organization membership, role permissions, departments, and category configuration.

Stage 3: Implement issue submission, persistence, and the reporter dashboard.

Stage 4: Implement routing, supervisor review, rejection, and requests for information.

Stage 5: Implement work assignments, technician updates, conditional finance approval, and verification.

Stage 6: Implement notifications, audit history, escalation, and operational analytics.

Stage 7: Test the complete workflows, improve reliability, and prepare a release candidate.

Stage 8: Integrate and verify the build pipeline, code-quality checks, Docker packaging, and deployment configuration.

Do not attempt to implement all stages in a single large code-generation pass.

## 17. Immediate instruction

Begin with a read-only repository audit.

Inspect the current project, run it where possible, examine the existing tests, and report the actual state of the code.

Then produce:
1. A concise architecture assessment.
2. A list of working and incomplete features.
3. A research-informed product improvement plan.
4. The highest-priority implementation gaps.
5. A small, testable first task.

Do not perform a broad rewrite during the audit.

Wait for confirmation before making major architectural changes.

The objective is to develop ProcessHub as a credible, differentiated, maintainable software product whose features work end to end and whose release readiness is demonstrated by evidence—not by appearance or unsupported claims.
