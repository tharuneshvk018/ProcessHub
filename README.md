# ProcessHub

> Intelligent problem reporting, workflow coordination, approval, and resolution platform.

ProcessHub is an enterprise-grade software platform built for organizations to manage operational requests, maintenance problems, multi-tier approvals, and verified field resolutions across diverse departments—including Facilities, Electrical, Plumbing, IT, Laboratory Support, Administration, and Finance.

---

## 1. Key Capabilities & Differentiators

ProcessHub replaces scattered emails, phone calls, and generic ticket lists with a unified, accountable workflow engine:

- **Context-Aware Dynamic Routing**: Issues are automatically routed based on category, subtype, campus/building location, asset identification, and severity—not just static category labels.
- **Explainable Workflow Progress**: Eliminates ambiguous statuses. Both reporters and managers see the exact current stage, blocking reasons (e.g., awaiting $380 quote approval), responsible roles, and countdown to SLA escalation.
- **Recurring-Issue Intelligence**: Correlates incoming reports against rolling 90-day asset and location history, alerting supervisors and technicians to repeat equipment failures (`CHRONIC_REPEAT`) and recommending overhaul over patch repairs.
- **Resolution Accountability & Independent Verification**: Technicians record repair notes, root cause, labor hours, parts consumed, and completion evidence. Work cannot be prematurely closed; independent verification (separation of duties) is strictly enforced before archival.
- **Conditional Financial Approvals**: Spend threshold evaluation ($100 limit); requests &le; $100 are auto-approved instantly without operational delay; requests &gt; $100 route to dedicated finance authorization queues.
- **Multi-Tier Approvals & Exception Handling**: Transparent handling of requests for information (RFI), mandatory rejection rationales, and automated fallback to `UNASSIGNED_TRIAGE` when reviewers are unconfigured.

---

## 2. System Architecture

```
Frontend (React 18 / Vite / Obsidian Enterprise UI / Nginx)
       │ (REST APIs / JWT Bearer Tokens / Port 3000 -> 80)
       ▼
Backend Core (Spring Boot 3.3.5 / Java 21+ / Port 8080)
 ├── Security & RBAC (Spring Security, BCrypt, Stateless JWT, Method Guards)
 ├── Context-Aware Dynamic Routing Engine
 ├── Explainable Progress & Workflow State Machine
 ├── Technician Workboard & Conditional Spend Engine ($100 Threshold)
 ├── Separation of Duties & Independent Verification Engine
 ├── Recurring-Issue Intelligence Service (Rolling 90-day Correlation)
 ├── Operational Analytics & SLA Escalation Service
 ├── In-App Persistent Notification Center
 └── Relational Persistence (PostgreSQL 16 / Flyway V1–V5 Migrations)
```

---

## 3. User Roles & Permission Hierarchy

1. **Reporter (`ROLE_REPORTER`)**: Submits structured issues with location, asset, and category details; tracks explainable progress; responds to information requests (RFI); verifies simple resolutions.
2. **Supervisor (`ROLE_SUPERVISOR`)**: Triages routed issues in supervised departments, initiates review, requests additional details, approves or rejects with mandatory rationale, and assigns work orders to technicians with live workload counters.
3. **Technician (`ROLE_TECHNICIAN`)**: Receives assigned work orders in priority order (`CRITICAL` &rarr; `LOW`), logs diagnosis and progress %, requests expenditure authorization, and submits completion evidence. (Separation of duties prohibits technicians from verifying their own work).
4. **Finance Approver (`ROLE_FINANCE_APPROVER`)**: Reviews expenditure requests exceeding delegated thresholds ($100.00), authorizing budget allocation or declining with operational notes.
5. **Organization Administrator (`ROLE_ADMIN`)**: Configures organizations, departments, user memberships, category routing rules, SLA policies, and investigates unrouted exceptions.

---

## 4. Getting Started & Operations

### Prerequisites
- **Java Development Kit**: JDK 21+ LTS (Tested on JDK 25 LTS)
- **Node.js**: v20+ (Tested on Node v22.19 with npm 10.9)
- **Docker & Docker Compose**: (Optional, for containerized multi-container deployment)
- **PostgreSQL**: v15+ (Optional; automated tests use isolated H2 PostgreSQL mode)

### Option A: One-Command Docker Compose Deployment
Run the complete multi-tier enterprise stack (PostgreSQL + Spring Boot Backend + React Frontend):
```bash
# 1. Copy environment template
cp .env.example .env

# 2. Build and launch all services in detached mode
docker compose up --build -d

# 3. Access web application
# Frontend UI: http://localhost:3000
# Backend API: http://localhost:8085/api/v1
```

### Option B: Local Development
#### 1. Start Backend Service
```bash
cd backend
# Run automated database migrations and launch Spring Boot API
./mvnw spring-boot:run
```
Backend API will be active on `http://localhost:8085`.

#### 2. Start Frontend Application
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:3000` in your web browser. The frontend provides genuine authenticated multi-user login with quick-selector credentials across the 5 enterprise personas:
- **Priya Sharma** (Reporter): `priya.sharma@processhub.com` / `Password123!`
- **Arjun Kumar** (Supervisor): `arjun.kumar@processhub.com` / `Password123!`
- **Karthik Raj** (Technician): `karthik.raj@processhub.com` / `Password123!`
- **Divya Nair** (Finance Approver): `divya.nair@processhub.com` / `Password123!`
- **Rahul Verma** (Administrator): `rahul.verma@processhub.com` / `Password123!`

---

## 5. Automated Verification & Testing

The entire system is backed by comprehensive automated test suites:
```bash
# Run all 126 automated backend tests (Unit, Integration, Security, Admin, and E2E Scenarios)
cd backend
./mvnw test

# Build production frontend bundle
cd frontend
npm run build
```

### End-to-End Test Suite Coverage (`EndToEndWorkflowScenariosIntegrationTest`):
1. **Scenario 1**: Direct maintenance repair without finance approval (auto-approved spend under threshold, independent verification, closed)
2. **Scenario 2**: Maintenance repair requiring conditional finance approval (spend > $100 transitions to `AWAITING_FINANCE`, finance approves, unblocks to `IN_PROGRESS`, resolved & verified)
3. **Scenario 3**: Rejected report with recorded reason (mandatory rejection reason validated, timeline updated)
4. **Scenario 4**: Request for information (RFI) dialogue and subsequent reporter resubmission (triage unblocked)
5. **Scenario 5**: Correct routing between IT and Facilities (queue isolation between departments verified)
6. **Scenario 6**: Role guard prevents unauthorized approval/rejection attempts (403 Forbidden enforced)
7. **Scenario 7**: Missing reviewer configuration (unstaffed department safely routes to `UNASSIGNED_TRIAGE` and alerts admin)
8. **Scenario 8**: Work completed but awaiting verification (separation of duties: technician cannot self-verify, 403 Forbidden)
9. **Scenario 9**: Missed deadline and escalation (`/analytics/sla-escalate` flags breach and dispatches escalation alerts)
10. **Scenario 10**: Recurring issue with accessible historical records (correlates repeat failures, calculates `CHRONIC_REPEAT`, provides overhaul advice)

---

## 6. Project Status
- **Current Milestone**: Stage 9 Complete & Verified.
- **Automated Tests**: 126 / 126 passing (0 failures, 0 errors).
- **Release Status**: Release Candidate (RC) Enterprise Ready.
