# AGENTS.md — Persistent Repository Development Guidelines

## 1. Product Identity and Brand Standards
- **Product Name**: ProcessHub.
- **Product Category**: Intelligent problem reporting, workflow coordination, approval, and resolution platform.
- **Professional Framing**: ProcessHub is an independent enterprise software platform designed for real operational environments (facilities, campus operations, IT, laboratories, and administrative organizations).
- **Prohibited Terminology**: Never frame or refer to the product in code, comments, documentation, commit messages, or user interfaces as a student project, academic exercise, demo prototype, or university assignment. Maintain enterprise-grade terminology and design at all times.

## 2. Engineering Discipline & Standards
- **Repository Integrity**: Inspect existing codebase and git status before making changes. Avoid broad, destructive rewrites; favor incremental, test-backed enhancements.
- **Source of Truth**: The active repository code and documentation represent current system truth. Do not make speculative claims about capabilities that have not been implemented and tested.
- **Documentation Maintenance**: After each milestone, keep `PROJECT_PROGRESS.md`, `ARCHITECTURE.md`, `README.md`, and `report.txt` updated with verified progress, architectural decisions, and execution logs.
- **Security by Design**:
  - Enforce backend role-based access control (RBAC) and department data isolation on every endpoint.
  - Never trust client-side role assertions or route-level guards alone.
  - No plaintext credentials, hardcoded secrets, or unvetted external resources.
  - Secure attachments through authenticated streaming endpoints with MIME-type and size validation.

## 3. Workflow & Business Logic Rules
- **State Integrity**: All workflow transitions (Submissions, Approvals, Assignments, Progress Updates, Expenditure Requests, Verifications, Closures, Escalations) must be validated and executed transactionally on the backend.
- **Auditability**: Every issue lifecycle event must generate an immutable audit record containing the actor ID, action type, timestamp, previous state, new state, and relevant reason/notes.
- **Explainability**: Every issue state must resolve to an explicit human-readable stage, current blocker, responsible party/role, and required next action. Ambiguous states like "Pending" without ownership are prohibited.
- **Accountability & Separation of Duties**: The technician executing a resolution cannot independently verify and close their own work when the configured verification policy mandates supervisor or reporter sign-off.
- **Graceful Exception Handling**: Define auditable fallback paths for unconfigured reviewers, rejected expenditures, requests for information (RFI), and missed SLAs.

## 4. Code Quality & Testing Requirements
- **Automated Verification**: Every service method, workflow transition, and API endpoint must be accompanied by automated unit or integration tests.
- **Testing Scope**: Verify happy paths, authorization boundaries, invalid transitions, concurrency/duplicate request handling, and exception flows.
- **Zero Cosmetic Placeholders**: UI actions must connect to functional backend handlers or be explicitly marked as disabled/unavailable. Decorative buttons with dead links are strictly forbidden.
