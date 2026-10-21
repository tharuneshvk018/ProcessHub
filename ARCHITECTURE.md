# ARCHITECTURE.md — ProcessHub System Architecture & Technical Design

## 1. Executive Summary & Purpose
ProcessHub is an enterprise-grade platform for intelligent problem reporting, workflow coordination, multi-tier approvals, and verified resolution. It bridges the gap between disorganized frontline problem reporting (facilities, electrical, plumbing, IT, lab operations) and structured operational execution with full accountability, explainable progress, and recurring-issue intelligence.

---

## 2. Technology Stack & Component Boundaries

### 2.1 Backend Architecture
- **Language & Runtime**: Java 25 LTS.
- **Framework**: Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA, Spring Validation).
- **Security & Authorization**: Spring Security with stateless JWT (JSON Web Tokens), BCrypt password hashing, and role/department-level method security (`@PreAuthorize`).
- **Database & Persistence**: Relational storage (PostgreSQL in production; robust schema migrations managed via Flyway/Liquibase; in-memory/test configuration for isolated CI test runs).
- **API Documentation**: OpenAPI 3.0 / Swagger UI.
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc, AssertJ.

### 2.2 Frontend Architecture
- **Framework**: React 18+ with modern JavaScript / TypeScript.
- **Styling**: Tailored, responsive design system built with custom CSS variables, accessible color contrast, micro-animations, and glassmorphism accents.
- **State Management & Routing**: React Router for view navigation; centralized API client with token interceptors and session refresh.
- **Component Design**: Role-oriented dashboards (Reporter, Supervisor, Technician, Finance Approver, Administrator) consuming unified workflow APIs.

---

## 3. High-Level Architecture & Layering

```
+-----------------------------------------------------------------------------------+
|                                Client Applications                                |
|  - Reporter Portal   - Supervisor Queue   - Tech Workboard   - Admin Center       |
+-----------------------------------------------------------------------------------+
                                          | HTTPS / REST / JSON
                                          v
+-----------------------------------------------------------------------------------+
|                             API & Controller Layer                                |
|  - AuthController    - IssueController    - WorkflowController - AdminController  |
|  - Token Validation  - Input Validation   - Rate Limiting      - Global Errors    |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                               Application Services                                |
|  - AuthenticationService                  - IssueService                          |
|  - ContextAwareRoutingService             - WorkflowExecutionEngine               |
|  - RecurringIssueIntelligenceService      - NotificationService                   |
|  - AuditLoggingService                    - StorageService                        |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                        Domain Models & Persistence Layer                          |
|  - Organization / Department / User / Role / Permission                           |
|  - Issue / WorkflowInstance / WorkflowTask / ExpenditureRequest                   |
|  - IssueTimeline / AuditLog / Notification                                        |
+-----------------------------------------------------------------------------------+
                                          | JDBC / JPA
                                          v
+-----------------------------------------------------------------------------------+
|                           Relational Database Storage                             |
+-----------------------------------------------------------------------------------+
```

---

## 4. Key Differentiators & Core Engines

### 4.1 Context-Aware Routing Engine
Unlike legacy service desks that route strictly based on static category names or arbitrary dropdown selections, ProcessHub evaluates a multi-factor routing matrix:
1. **Category & Subtype**: e.g., `FACILITIES -> WATER_LEAKAGE` or `IT -> NETWORK_OUTAGE`.
2. **Location & Impact Zone**: Campus, Building, Floor, Room/Zone.
3. **Asset Identifier**: Correlates the issue to a tracked piece of equipment or infrastructure.
4. **Severity & Safety Indicator**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL_SAFETY`.
5. **Supervisor Registry**: Dynamically resolves the active department supervisor responsible for the target domain and zone.
6. **Exception Handling**: If no eligible supervisor or technician team is registered, the issue is routed to an `UNASSIGNED_TRIAGE` state, creating an actionable exception notification for Organization Administrators.

### 4.2 Explainable Workflow Progress Engine
ProcessHub resolves the "black hole" problem where users cannot determine why an issue is delayed. Every issue record dynamically computes an `ExplainableProgressDTO`:
- **Current Lifecycle Stage**: e.g., `UNDER_SUPERVISOR_REVIEW`, `AWAITING_FINANCE_APPROVAL`, `IN_REPAIR`, `PENDING_VERIFICATION`.
- **Current Blocking Reason**: e.g., *"Expenditure authorization requested by Technician ($450.00 for replacement valve) exceeds auto-approval threshold."*
- **Responsible Party / Role**: e.g., *"Finance Approver (Finance Department)"*.
- **Required Next Action**: e.g., *"Review expenditure quote and approve or decline with notes."*
- **SLA Deadline & Escalation Countdown**: Time remaining before automated supervisor escalation occurs.

### 4.3 Recurring-Issue Intelligence Service
To prevent repetitive, symptomatic fixes on failing equipment or chronic infrastructure faults:
- Upon submission and during triage, the engine queries recent issue history (configurable rolling window, e.g., 90 days) matching:
  - Exact Asset ID match.
  - Same Location (Building + Room) within the same category.
  - Symptom keyword similarity in title/description.
- The supervisor and assigned technician are presented with an embedded **Recurrence Intelligence Card**:
  - Total recurrences in past 90 days.
  - References to past closed issues (`PH-1021`, `PH-1088`).
  - Previous resolutions and technicians involved.
  - Recommendation flag (e.g., *"Repeat failure detected: Consider full replacement rather than repeated patch repair"*).

### 4.4 Resolution Accountability & Independent Verification
To eliminate false closures:
- A technician cannot unilaterally mark an issue `CLOSED`.
- Upon completing work, the technician must submit:
  - Work summary and root cause found.
  - Labor hours and parts used.
  - Optional photographic completion evidence.
- The issue transitions to `RESOLVED_PENDING_VERIFICATION`.
- Verification must be performed by an authorized party (Supervisor or original Reporter, depending on organization policy). If verification fails, the issue is reopened to `WORK_IN_PROGRESS` with documented failure notes.

---

## 5. Domain Model & Entity Relationships

### 5.1 Identity & Access Management (IAM)
- **Organization**: Multi-tenant container (`id`, `name`, `code`, `status`, `settings`).
- **Department**: Organizational units (`id`, `org_id`, `name`, `code`, `is_active`).
- **User**: User accounts (`id`, `email`, `password_hash`, `full_name`, `phone`, `is_active`).
- **Membership**: Links users to organizations and departments with specific roles (`USER_REPORTER`, `SUPERVISOR`, `TECHNICIAN`, `FINANCE_APPROVER`, `ORG_ADMIN`).

### 5.2 Problem Reporting & Workflows
- **Issue**: The primary operational record:
  - `id`, `reference_code` (e.g., `PH-1001`), `org_id`, `department_id`, `reporter_id`
  - `category`, `subtype`, `title`, `description`
  - `location_building`, `location_room`, `asset_id`
  - `priority` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
  - `status` (`SUBMITTED`, `UNDER_REVIEW`, `MORE_INFO_REQUESTED`, `ASSIGNED`, `IN_PROGRESS`, `AWAITING_FINANCE`, `RESOLVED_PENDING_VERIFICATION`, `CLOSED`, `REJECTED`, `REOPENED`)
  - `assigned_technician_id`, `created_at`, `updated_at`, `closed_at`
- **WorkflowInstance**: Maintains state-machine lifecycle, transition history, and SLA timers.
- **ExpenditureRequest**: Tracks financial approvals when repairs exceed authorized limits:
  - `id`, `issue_id`, `requested_by_id`, `amount`, `justification`, `status` (`PENDING`, `APPROVED`, `REJECTED`), `decided_by_id`, `decision_notes`.
- **IssueTimeline**: Immutable audit log of all events (`actor_id`, `action`, `from_status`, `to_status`, `notes`, `timestamp`).
- **Notification**: In-app notifications with read/unread tracking, issue link, and priority badge.

---

## 6. Security & Data Protection
1. **Backend Authorization Guards**: Every API endpoint uses `@PreAuthorize` evaluating tenant membership, role, and department.
2. **Tenant Data Isolation**: All repository queries include `org_id` criteria, preventing cross-tenant access via identifier tampering.
3. **Password Security**: BCrypt hashing with strong work factor; no credentials in source code.
4. **Input Sanitization**: Strong validation using Jakarta Validation (`@NotNull`, `@Size`, `@Pattern`) and XSS filtering.
