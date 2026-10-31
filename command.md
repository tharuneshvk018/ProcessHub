# PROCESSHUB — FINAL WORKFLOW INTEGRATION, NOTIFICATION FIXES AND CODEBASE CLEANUP

## 1. Project context

Project: ProcessHub  
Description: Intelligent Workflow & Resolution Platform  
Existing workspace: D:\ProcessHub

The application has already been developed. This is an update to the existing project, not a request to rebuild it.

Your responsibility is to analyze the existing codebase and improve the integration between the five user roles, workflow tracking, notifications, administrative monitoring and application reliability.

**CRITICAL INSTRUCTION: Modify only what is necessary to implement the requirements below. Preserve all existing working features, UI design, theme, layouts, components, routes, business logic and project structure unless a specific change is required.**

Do not redesign the application. Do not replace existing implementations with unnecessary alternatives. Do not introduce unrelated features.

---

# 2. First: Perform a complete existing-project audit

Before making changes, inspect the entire project.

Analyze:

- Frontend components, pages, routes and navigation.
- Backend controllers, services, repositories and entities.
- Database tables, relationships and migrations.
- Authentication and authorization.
- Existing five user roles and individual user accounts.
- Issue reporting and workflow state transitions.
- Notification creation, delivery and notification history.
- Tracking and audit timeline implementation.
- Admin dashboard and reporting functionality.
- Existing API integrations and frontend API calls.
- Git, Maven, JUnit, Docker and Jenkins configurations.
- Existing test cases and their current results.

Identify the actual reasons why:

1. Workflow tracking is not updating consistently.
2. Notifications are not being delivered to the corresponding users.
3. User activities are not integrated across all roles.
4. The issue lifecycle is not consistently reflected in every dashboard.
5. Any frontend and backend components are not communicating correctly.

Prepare a short internal audit identifying the affected files, root causes and required modifications.

Do not start rewriting the project before understanding the existing implementation.

# 3. Five individual users and role-based collaboration

The application must demonstrate five separate users working collaboratively.

The five roles are:

| User | Responsibility |
|---|---|
| Reporter | Reports problems and tracks submitted issues |
| Supervisor | Reviews reports, assigns work and verifies completion |
| Technician | Receives assigned work and performs repairs |
| Finance Approver | Reviews and approves or rejects expenditure requests |
| Administrator | Monitors users, system activities and overall reports |

## Individual user behavior

Each role must have its own individual authenticated account.

When a user logs in:

- The correct user identity must be displayed.
- The correct role-specific dashboard must load.
- Only the actions permitted for that role must be accessible.
- The user must see their own notifications.
- Their actions must be recorded under their own identity.
- Their activity must be reflected in the shared issue workflow where relevant.

All five users must work with the same underlying issue records and shared workflow state.

Do not implement five isolated demo dashboards with unrelated static data.

Do not use frontend-only role switching as a substitute for actual authentication and authorization.

Do not allow users to perform another role's protected operations by changing a dropdown or modifying frontend state.

Preserve existing authentication and account-management behavior wherever possible.

# 4. Complete integrated workflow

Implement and verify the following end-to-end process.

## Stage 1 — Reporter submits a problem

The reporter logs in and submits a problem containing:

- Problem title.
- Description.
- Department and category.
- Priority or severity.
- Building, room or location.
- Asset information where applicable.

After submission:

1. Save the issue in the shared database.
2. Generate or retain its unique reference number.
3. Set the correct initial workflow status.
4. Record the submission event in the issue timeline.
5. Notify the appropriate supervisor.
6. Make the issue visible in the reporter's dashboard.
7. Allow the reporter to track the issue.

The reporter must receive a notification confirming successful submission.

## Stage 2 — Supervisor receives and reviews the report

The supervisor logs in.

The supervisor must see reports that are assigned or routed to their authorized review queue.

When a new issue is submitted:

- Create a notification for the corresponding supervisor.
- Show it in that supervisor's notification history.
- Display the issue in the supervisor's pending-review queue.
- Allow the supervisor to inspect the complete issue details.

The supervisor must be able to:

- Approve the report.
- Reject the report with a reason.
- Request additional information.
- Assign the approved work to an appropriate technician.

After a decision:

- Update the shared issue status.
- Record the decision and responsible supervisor in the timeline.
- Notify the reporter about the review result.
- If work is assigned, notify the selected technician.
- Update the relevant dashboards immediately or through reliable data refresh.

The reporter must be able to see whether the report is under review, approved, rejected or awaiting additional information.

## Stage 3 — Technician receives assigned work

When the supervisor assigns a task:

1. Save the technician assignment in the database.
2. Update the issue's current workflow stage.
3. Record the assignment event in the audit timeline.
4. Generate a notification for the selected technician.
5. Show the assigned task in that technician's workboard.
6. Notify the reporter that the issue has moved to the assigned or repair stage.

The technician must see only tasks assigned to their account or otherwise authorized work.

The technician must be able to:

- Open the assigned issue.
- Review its description, priority, location and asset.
- Start work.
- Update repair progress.
- Record diagnosis and work details.
- Submit an expenditure request when necessary.
- Mark the work as completed when the repair is finished.

Every meaningful action must update the shared issue record and timeline.

## Stage 4 — Conditional finance approval

Finance approval must be conditional.

If a technician requests expenditure that requires financial authorization:

1. Create an expenditure request linked to the issue.
2. Store the amount and justification.
3. Set the issue or work item to an appropriate finance-pending state.
4. Notify the authorized finance approver.
5. Display the request in the finance user's queue.
6. Prevent the relevant expenditure-dependent work from proceeding until approval is granted.

The finance approver must be able to:

- Review the request.
- Approve it.
- Reject it with a reason.

After the decision:

- Save the finance decision.
- Record the approver and timestamp.
- Update the issue workflow.
- Notify the technician about the decision.
- Notify the supervisor and reporter where the decision materially changes the issue status.
- Allow the technician to continue only when the required approval has been granted.

If finance approval is not required, the workflow must continue without introducing an unnecessary finance stage.

Do not force every issue through the finance queue.

Important: Finance approval is an authorization gate for expenditure. It must occur before the relevant spending takes place, not be treated as an automatic approval after the repair is completed.

## Stage 5 — Technician completes the work

After the repair:

- Technician records the completion summary.
- Technician records the diagnosis or root cause where applicable.
- Technician records relevant labor and material details.
- Technician attaches completion evidence if supported by the existing application.
- The issue transitions to a resolved-pending-verification state.
- The completion event is added to the audit timeline.

Notify the supervisor that the work is ready for verification.

Notify the reporter that the repair has been completed and is awaiting verification.

Do not close the issue automatically when the technician clicks complete.

## Stage 6 — Supervisor independently verifies the result

The supervisor receives a notification that the technician has submitted the completed work.

The supervisor must be able to inspect:

- Original problem report.
- Technician's diagnosis.
- Work completion summary.
- Expenditure details and approval, if applicable.
- Completion evidence, if available.
- Full workflow timeline.

The supervisor must be able to:

1. Verify and accept the completed work.
2. Reject verification and reopen the issue with a reason.

If accepted:

- Set the issue to the final closed or verified-closed state.
- Record the verifying supervisor and timestamp.
- Notify the reporter about successful closure.
- Notify the technician about verification completion.
- Update all relevant dashboards and analytics.

If rejected:

- Reopen or return the issue to the appropriate work stage.
- Record the rejection reason.
- Notify the technician and reporter.
- Make the required next action visible.

Enforce separation of duties using the existing authorization model. Do not allow an unauthorized user to verify work.

# 5. Individual notification system

This is a critical requirement.

Currently, notification behavior is not sufficiently integrated. Correct the existing notification implementation.

Every notification must be associated with its intended recipient.

A notification record should contain, as appropriate to the existing design:

- Notification ID.
- Recipient user ID.
- Related issue ID.
- Notification type.
- Notification title.
- Notification message.
- Created timestamp.
- Read/unread status.
- Relevant navigation destination.

## Recipient-specific notification behavior

Notifications must be generated according to the actual workflow event.

Examples:

| Event | Notification recipient |
|---|---|
| New problem submitted | Appropriate supervisor |
| Submission confirmation | Reporter |
| Supervisor approves or rejects | Reporter |
| Additional information requested | Reporter |
| Technician assigned | Assigned technician |
| Assignment confirmation | Reporter |
| Technician starts work | Reporter and relevant supervisor |
| Expenditure requested | Finance approver |
| Finance approves or rejects | Technician, supervisor and reporter as appropriate |
| Technician completes work | Supervisor and reporter |
| Verification accepted | Reporter and technician |
| Verification rejected | Technician and reporter |
| Issue closed | Reporter and relevant participants |

Do not send every notification to every user.

Do not display all system notifications in every user's notification history.

Each authenticated user must receive only notifications intended for their account, subject to any explicitly authorized administrative monitoring functionality.

## Notification history

Each user must be able to:

- View their own notifications.
- Identify read and unread notifications.
- Open the associated issue from a notification.
- See when the notification was generated.
- Mark notifications as read.
- Access their notification history.

Unread counts must be calculated for the logged-in user, not globally.

When a notification is opened, update its read status for that recipient.

Use reliable backend filtering by authenticated user ID. Do not rely only on frontend filtering.

Prevent one user from accessing another user's notification history through API parameter manipulation.

# 6. Integrated issue tracking and audit timeline

Currently, issue tracking is not working consistently.

Investigate and repair the complete tracking mechanism.

The issue timeline must represent the actual sequence of events, not hardcoded demonstration entries.

Record relevant events such as:

- Issue submitted.
- Supervisor review started.
- Approval or rejection.
- Information requested.
- Technician assignment.
- Technician accepted or started work.
- Progress update.
- Expenditure requested.
- Finance approval or rejection.
- Work completed.
- Verification accepted or rejected.
- Issue closed or reopened.

Each event should contain:

- Issue reference.
- Event type.
- Actor's user ID and display name.
- Actor's role.
- Timestamp.
- Relevant description.
- Previous and new status where applicable.

## Tracking consistency

The same issue must show consistent information in:

- Reporter dashboard.
- Supervisor queue.
- Technician workboard.
- Finance queue when applicable.
- Verification page.
- Administrator reports.

When one user updates an issue, the change must be reflected for other authorized users.

Use the existing API and data-fetching approach where possible. Implement appropriate refresh or update mechanisms without unnecessarily introducing complex real-time infrastructure.

Avoid inconsistent statuses between the frontend and backend.

The backend must be the authoritative source for workflow state.

# 7. Administrator monitoring and reports

The administrator must have system-wide monitoring capabilities.

The administrator should be able to view:

## User activity monitoring

- Registered users.
- User identities and roles.
- Relevant account status.
- Recent user activities.
- Important actions performed by each user.
- Issue submissions, reviews, assignments, decisions and closures.
- Activity timestamps.

## Overall system reports

- Total reported issues.
- Issues by current status.
- Issues by department.
- Issues by priority.
- Pending reviews.
- Assigned and active work.
- Issues awaiting finance approval.
- Issues awaiting verification.
- Verified and closed issues.
- Reopened issues.
- SLA indicators and overdue cases, if supported by the existing system.

Administrator monitoring must be read-only for audit and reporting purposes unless the existing application already provides specific authorized management operations.

Do not expose passwords, JWT tokens, private credentials or other sensitive authentication data.

Admin monitoring must not silently change workflow records.

# 8. Backend and frontend integration requirements

Inspect the existing API contracts and correct mismatches.

Verify:

- Every frontend API call uses the correct backend endpoint.
- Correct HTTP methods are used.
- Authentication tokens are passed where required.
- Backend authorization checks are enforced.
- Request and response models match.
- User IDs and issue IDs are consistent.
- Database changes are persisted correctly.
- Error responses are handled clearly.
- Dashboard counts are based on actual stored data.
- Notification records are persisted and retrieved correctly.
- Workflow transitions are validated on the backend.

Use database transactions for operations that must update multiple related records together, such as an assignment, workflow transition, audit event and notification creation.

Prevent duplicate notifications when the same operation is retried.

Use appropriate constraints and validation to prevent invalid assignments, duplicate actions and unauthorized status changes.

Do not introduce a new framework or replace the current architecture just to solve an integration problem.

# 9. Project code cleanup and unused file analysis

Perform a careful codebase cleanup after the main integration changes.

The objective is to remove genuinely unused code and files without breaking the application.

Inspect:

- Unused React components.
- Unused pages and routes.
- Unused CSS and style files.
- Unused Java classes.
- Unused methods and services.
- Unused controllers and repositories.
- Unused dependencies.
- Duplicate components.
- Old test files that are no longer relevant.
- Unreferenced assets and images.
- Temporary scripts and generated files.
- Obsolete mock data and demonstration code.
- Old configuration files that are no longer used.

## Cleanup safety rules

Do not delete files simply because they appear unused in one search result.

Before removing anything:

1. Search all imports and references.
2. Check dynamic imports and route registration.
3. Check dependency injection and Spring component scanning.
4. Check reflection, configuration and resource loading.
5. Check build scripts and Docker configuration.
6. Check tests and runtime dependencies.
7. Identify whether the file is required by the application or deployment environment.

Create a backup or a dedicated Git commit before destructive cleanup.

If a file's usage is uncertain, retain it and report it instead of deleting it.

Do not remove:

- Maven Wrapper files.
- Database migrations.
- Required environment configuration.
- Docker configuration.
- Jenkins configuration.
- Required application resources.
- Tests needed to validate the project.
- Documentation that is still relevant.
- Existing working modules.

Do not remove useful dependencies or files merely to reduce project size.

After cleanup, verify that the complete project still builds and runs.

# 10. Testing requirements

Test the entire application using five separate user accounts.

Use the existing test framework and project configuration.

At minimum, verify the following scenarios:

### Authentication and permissions
- Each of the five roles can authenticate independently.
- Each user receives the correct dashboard.
- Unauthorized actions are rejected.
- Users cannot access other users' notification histories.

### End-to-end workflow
- Reporter submits an issue.
- Supervisor receives the notification.
- Supervisor reviews and assigns a technician.
- Assigned technician receives the notification.
- Technician updates work progress.
- Finance receives a notification when an approval is required.
- Finance approves or rejects the request.
- Technician receives the decision.
- Technician completes the work.
- Supervisor receives the completion notification.
- Supervisor verifies or rejects the result.
- Reporter receives the final status notification.

### Tracking and notifications
- Every workflow event appears in the correct issue timeline.
- Notifications are delivered only to intended recipients.
- Read/unread status works independently for each user.
- Dashboard counts update after workflow changes.
- Notification history remains associated with the correct user.
- Duplicate notifications are not generated for a single successful action.

### Administrator
- Administrator can view authorized user activity.
- Administrator can view overall reports.
- Administrator cannot access secret authentication information.
- Monitoring does not accidentally modify workflow state.

### Regression testing
- Existing registration and login still work.
- Existing issue reporting still works.
- Existing dashboards still work.
- Existing design and navigation remain intact.
- Existing working features are not broken.

Run the available Maven and frontend test/build commands.

Report actual test results. Never invent successful results or suppress test failures.

# 11. Execution strategy

Work in controlled stages.

**Stage A — Audit**
- Inspect the existing implementation.
- Identify root causes.
- Prepare a concise list of affected modules and files.

**Stage B — Workflow integration**
- Correct the shared issue lifecycle.
- Correct role-based actions and assignments.
- Ensure backend persistence and consistent status transitions.

**Stage C — Notifications**
- Implement recipient-specific notifications.
- Fix notification history and read/unread behavior.
- Test notifications independently for all five users.

**Stage D — Tracking**
- Correct the shared audit timeline.
- Ensure all relevant dashboards display consistent workflow state.

**Stage E — Administrator**
- Integrate user activity monitoring and system-wide reports.

**Stage F — Code cleanup**
- Identify and remove only verified unused files and code.
- Preserve uncertain or required components.

**Stage G — Full testing**
- Run five-user end-to-end workflow tests.
- Run regression tests.
- Verify the project builds and can be demonstrated.

Do not skip directly to cleanup before stabilizing the workflow.

# 12. Strict preservation instructions

These instructions override any tendency to redesign or expand the application.

- Keep the current white theme and visual identity.
- Keep the current page layouts and navigation.
- Keep existing working components.
- Keep existing product naming and branding.
- Keep existing technology choices.
- Do not create a new application.
- Do not replace the database unnecessarily.
- Do not add unrelated features.
- Do not over-engineer real-time communication.
- Do not convert this into a large enterprise SaaS platform.
- Do not add new roles beyond the five specified roles.
- Do not replace working mock-independent functionality with new mock data.
- Do not claim completion without testing.

Make only the changes necessary to achieve a smooth, consistent and demonstrable five-user collaborative workflow.

# 13. Final report required

After completing the work, provide a concise but detailed report containing:

1. Root causes identified.
2. Files and modules modified.
3. Workflow changes implemented.
4. Notification changes implemented.
5. Tracking and audit changes implemented.
6. Administrator functionality implemented.
7. Unused files and code safely removed.
8. Files retained because their usage was uncertain.
9. Backend test results.
10. Frontend test and build results.
11. Five-user end-to-end test results.
12. Remaining issues, if any.
13. Exact commands to run the application.
14. Exact steps to demonstrate the complete workflow.

Also update the existing project progress documentation to reflect the actual implementation.

**Final objective:** ProcessHub must operate as one integrated application in which five individual users collaborate through a shared, persistent issue workflow, receive only their relevant notifications, see accurate tracking information and allow the administrator to monitor overall system activity.

Begin with the audit. Preserve everything that already works. Implement only the requested changes and verify the result.