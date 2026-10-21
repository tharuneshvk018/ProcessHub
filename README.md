# ProcessHub

> Intelligent problem reporting, workflow coordination, approval, and resolution platform.

ProcessHub is a modern software platform built for organizations to manage operational requests, maintenance problems, multi-tier approvals, and verified field resolutions across diverse departments—including Facilities, Electrical, Plumbing, IT, Laboratory Support, Administration, and Finance.

---

## 1. Key Capabilities & Differentiators

ProcessHub replaces scattered emails, phone calls, and generic ticket lists with a unified, accountable workflow engine:

- **Context-Aware Dynamic Routing**: Issues are automatically routed based on category, subtype, campus/building location, asset identification, and severity—not just static category labels.
- **Explainable Workflow Progress**: Eliminates ambiguous statuses. Both reporters and managers see the exact current stage, blocking reasons (e.g., awaiting $450 quote approval), responsible roles, and countdown to SLA escalation.
- **Recurring-Issue Intelligence**: Correlates incoming reports against past asset and location history, alerting supervisors and technicians to repeat equipment failures and chronic defects.
- **Resolution Accountability & Independent Verification**: Technicians record repair notes, root cause, and completion evidence. Work cannot be prematurely closed; independent verification is enforced before archival.
- **Multi-Tier Approvals & Exception Handling**: Transparent handling of financial expenditure authorizations, requests for additional information (RFI), and automated fallbacks when reviewers are unavailable.

---

## 2. System Architecture

```
Frontend (React 18 / Tailored Modern UI)
       │ (REST APIs / JWT Auth)
       ▼
Backend Core (Spring Boot / Java 25 LTS)
 ├── Security & RBAC (Spring Security, BCrypt, Method Guards)
 ├── Context-Aware Routing Service
 ├── Workflow State Engine & Exception Coordinator
 ├── Recurring-Issue Intelligence Service
 ├── Notification & Audit Services
 └── Relational Persistence (PostgreSQL / Flyway Migrations)
```

---

## 3. User Roles & Permission Hierarchy

1. **Reporter**: Submits structured issues with location, asset, and category details; tracks explainable progress; responds to information requests; verifies simple resolutions.
2. **Supervisor / Department Reviewer**: Triages routed issues, requests additional details, approves or rejects with mandatory rationale, and assigns work orders to eligible technicians.
3. **Technician**: Receives assigned work orders, logs diagnosis and repair milestones, requests expenditure authorization when needed, and submits completion evidence.
4. **Finance Approver**: Reviews expenditure requests exceeding delegated thresholds, authorizing budget allocation or requesting modifications.
5. **Organization Administrator**: Configures organizations, departments, user memberships, category routing rules, SLA policies, and investigates unrouted exceptions.

---

## 4. Getting Started & Development

### Prerequisites
- **Java Development Kit**: JDK 25 LTS (or JDK 21+)
- **Node.js**: v20+ (v22 recommended) with npm
- **Database**: PostgreSQL 15+ (or H2 in-memory for local testing)

### Quick Start
1. **Clone and Setup Environment**:
   ```bash
   cp .env.example .env
   ```
2. **Backend Services**:
   ```bash
   # Navigate to backend and run with maven wrapper
   ./mvnw spring-boot:run
   ```
3. **Frontend Application**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 5. Implementation Roadmap & Verification

ProcessHub is developed strictly following the phased milestone directive outlined in `PROJECT_PROGRESS.md` and `command.md`. All milestone achievements are backed by automated tests and documented in `report.txt`.
