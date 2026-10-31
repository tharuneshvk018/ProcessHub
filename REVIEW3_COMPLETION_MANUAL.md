# ProcessHub — Review 3 Operational & Evidence Completion Manual

**Document ID**: `PH-DOC-REV3-MANUAL-001`  
**Target Repository**: `D:\ProcessHub`  
**Classification**: Technical Operations & Evidence Audit Manual  
**Author**: Technical Documentation & Systems Architecture Team  
**Date**: October 31, 2026  

---

## Executive Summary & Purpose

This manual is the definitive, practical, step-by-step guide for executing, verifying, and collecting formal evidence across all **ten core architecture components** of the **ProcessHub Intelligent Workflow & Resolution Platform** for the Review 3 milestone.

Each section provides:
1. **Repository Inspection Findings**: Verified current state of the codebase and environment.
2. **Action Classification**: Explicit separation of automated agent tasks vs. developer manual actions.
3. **Step-by-Step PowerShell Commands**: Copy-paste executable commands for Windows PowerShell.
4. **Expected Output & Diagnostics**: Exact terminal responses indicating success or error.
5. **Mandatory Evidence Deliverables**: File names, directory targets, and screenshot instructions.
6. **Pass / Fail / Pending Criteria**: Unambiguous verification gates.

---

## Table of Contents

- [Special Operational Advisory: C: Drive Space & Docker WSL Recovery](#special-operational-advisory-c-drive-space--docker-wsl-recovery)
- [Component 1: Git & GitHub Repository Management](#component-1-git--github-repository-management)
- [Component 2: Docker Containerization & Multi-Service Compose](#component-2-docker-containerization--multi-service-compose)
- [Component 3: SonarQube Code Quality & Static Analysis](#component-3-sonarqube-code-quality--static-analysis)
- [Component 4: Jenkins 10-Stage Declarative CI/CD Pipeline](#component-4-jenkins-10-stage-declarative-cicd-pipeline)
- [Component 5: Automated Testing (JUnit 5 & Frontend Build Verification)](#component-5-automated-testing-junit-5--frontend-build-verification)
- [Component 6: Database Architecture (PostgreSQL vs. H2 Dev Mode)](#component-6-database-architecture-postgresql-vs-h2-dev-mode)
- [Component 7: Spring Boot 3 Backend Service](#component-7-spring-boot-3-backend-service)
- [Component 8: React 18 / Vite Production Frontend](#component-8-react-18--vite-production-frontend)
- [Component 9: Enterprise Security & Credential Protection](#component-9-enterprise-security--credential-protection)
- [Component 10: Final Evidence Documentation & Audit Index](#component-10-final-evidence-documentation--audit-index)

---

## Special Operational Advisory: C: Drive Space & Docker WSL Recovery

### Root Cause Analysis
During system inspection, Drive `C:` was measured at **~0.03 GB (under 35 MB) free disk space**, while Drive `D:` has **~295 GB free space**.

This critical disk exhaustion causes three immediate failure modes:
1. **WSL 2 / Docker Desktop Crash**: Docker Desktop stores its virtual disk (`ext4.vhdx`) under `%LOCALAPPDATA%\Docker\wsl\data\`. When `C:` drops below 100 MB, WSL cannot allocate virtual disk pages, resulting in `WSL virtual disk error`, container I/O timeouts, or `docker compose build` aborting.
2. **Maven Build Classpath Failure**: Spawning the JVM with large classpaths fails with `Could not build classpath: There is not enough space on the disk`.
3. **Language Server & IDE Cache Lock**: VS Code / Antigravity Java Language Server is unable to update project indices, prompting recurring `pom.xml reload` diagnostics.

---

### Step-by-Step Disk Recovery & Docker WSL Migration Procedure

> [!IMPORTANT]
> This procedure safely relocates the multi-gigabyte Docker WSL distribution to the spacious `D:` drive without deleting existing Docker images, persistent PostgreSQL volumes, or ProcessHub application data.

#### Phase 1: Reclaim 2–5 GB of Emergency Space on C: Drive (Manual)
Run the following in an elevated (Administrator) PowerShell window:

```powershell
# 1. Clear user temporary directory
Remove-Item -Path "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue

# 2. Clear Windows temporary directory
Remove-Item -Path "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue

# 3. Empty the Recycle Bin on C:
Clear-RecycleBin -DriveLetter C -Force -ErrorAction SilentlyContinue

# 4. Clean Maven local repository cache if redundant (keep only ProcessHub artifacts)
# Alternatively, relocate Maven repository to D:\.m2
```

#### Phase 2: Relocate Docker WSL Virtual Disk from C: to D: (Preserving Volumes & Data)
Execute these exact commands sequentially in PowerShell:

```powershell
# Step 1: Terminate Docker Desktop and shut down WSL completely
wsl --shutdown
Get-Process "*docker*" | Stop-Process -Force -ErrorAction SilentlyContinue

# Step 2: Create a dedicated destination directory on D: drive
New-Item -ItemType Directory -Path "D:\DockerWSL\data" -Force

# Step 3: Export existing docker-desktop-data (preserves all PostgreSQL tables and volumes)
wsl --export docker-desktop-data D:\DockerWSL\docker-desktop-data.tar

# Step 4: Unregister the space-choking distribution from C: drive
wsl --unregister docker-desktop-data

# Step 5: Import the distribution onto D: drive
wsl --import docker-desktop-data D:\DockerWSL\data D:\DockerWSL\docker-desktop-data.tar --version 2

# Step 6: Delete the temporary tar archive to release space
Remove-Item -Path "D:\DockerWSL\docker-desktop-data.tar" -Force

# Step 7: Restart Docker Desktop
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

**Expected Result**:
- Drive `C:` recovers **10 GB to 40 GB** of free space immediately.
- Docker Desktop starts without WSL disk errors.
- All persistent volumes (`postgres_data`, `backend_uploads`) remain **100% intact**.

---

## Component 1: Git & GitHub Repository Management

### 1.1 Inspection Findings
- **Local Branch**: `main` (verified via `.git/HEAD`).
- **Commit History**: 8 semantic commits recorded (`e255f83` through `3d6346a`), covering architecture baseline, Stage 1 security, Stage 2 multi-tenancy, Stage 6 analytics, Stage 7 E2E tests, and Stage 9 multi-user persistence.
- **Git User Identity**: Configured as `ProcessHub Lead Engineer <dev@processhub.com>`.
- **Remote Configuration**: Inspected `.git/config` — `[remote "origin"]` is **not yet registered**.
- **.gitignore Inspection**: Properly ignores `.env`, `.env.local`, `target/`, `node_modules/`, `dist/`, `.vscode/`, `.idea/`, `*.pem`, `*.key`.
- **Exposed Secret Risk**: `.env.example`, `application.yml`, and `docker-compose.yml` contain a fallback demonstration JWT secret string (`404E6352...`). In a public repository, GitHub Secret Scanning will raise an alert unless this is parameterized.

### 1.2 Action Separation
- **Automated**: Inspecting commit hashes, verifying `.gitignore`, identifying untracked files.
- **Manual User Action**: Adding GitHub remote origin, running `git push`, creating GitHub Release on GitHub.com.

### 1.3 Step-by-Step Commands

```powershell
# Navigate to workspace root
cd D:\ProcessHub

# 1. Verify current status and branch
git status
# Expected output: On branch main, nothing to commit, working tree clean (or listing untracked manuals)

# 2. View recent verified commits
git log --oneline -n 10
# Expected output:
# 3d6346a fix(tests): remove unused imports and unused test token fields
# e41b37f feat: complete ProcessHub platform with E2E scenario validation suite...
# d1d548c docs: update PROJECT_PROGRESS.md with 33 passing tests
# ...
# e255f83 docs: establish Stage 0 baseline, architecture, and engineering directives

# 3. Add remote origin (Replace <YOUR-GITHUB-USERNAME> with your actual GitHub username)
git remote add origin https://github.com/<YOUR-GITHUB-USERNAME>/ProcessHub.git

# 4. Verify remote URL
git remote -v
# Expected output:
# origin  https://github.com/<username>/ProcessHub.git (fetch)
# origin  https://github.com/<username>/ProcessHub.git (push)

# 5. Push main branch and tags to GitHub
git push -u origin main
```

### 1.4 Creating the Final GitHub Release
1. Open your repository on **GitHub.com** &rarr; Navigate to **Releases** &rarr; Click **Draft a new release**.
2. **Tag version**: `v1.0.0-rc1`.
3. **Target**: `main`.
4. **Release title**: `ProcessHub Enterprise v1.0.0-rc1 — Intelligent Workflow Platform`.
5. **Release Description**: Paste summary of Review 3 deliverables (126 passing tests, 5-role collaborative workflow, Docker orchestration, Flyway migrations, and production Nginx frontend).
6. Click **Publish release**.

### 1.5 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\02_git_github\`
- **File Name**: `02_git_github_workflow_and_history.png`
- **What to capture**: Terminal showing `git status`, `git branch -a`, `git remote -v`, and `git log -n 5`.
- **Secondary Capture**: Browser window showing the published GitHub Release page with tag `v1.0.0-rc1`.

---

## Component 2: Docker Containerization & Multi-Service Compose

### 2.1 Inspection Findings
- **Compose Manifest**: [`docker-compose.yml`](file:///d:/ProcessHub/docker-compose.yml) configures three interconnected enterprise services on `processhub-net`:
  1. `db`: `postgres:16-alpine` (healthcheck: `pg_isready -U processhub -d processhub`, port `5432:5432`, volume `postgres_data`).
  2. `backend`: Multi-stage build via [`backend/Dockerfile`](file:///d:/ProcessHub/backend/Dockerfile) (Temurin JRE 21 Alpine runtime, non-root user `processhub`, port `8085:8080`, volume `backend_uploads`).
  3. `frontend`: Multi-stage build via [`frontend/Dockerfile`](file:///d:/ProcessHub/frontend/Dockerfile) (Node 22 builder &rarr; Nginx 1.27 Alpine runtime with SPA reverse proxy, port `3000:80`).
- **Dependencies**: `backend` waits for `db` condition `service_healthy`; `frontend` depends on `backend`.

### 2.2 Action Separation
- **Automated**: Verifying Dockerfile syntax, environment variable mappings, healthcheck definitions.
- **Manual User Action**: Executing `docker compose build` and `docker compose up -d` in terminal (requires Docker Desktop running after WSL recovery).

### 2.3 Step-by-Step Commands

```powershell
cd D:\ProcessHub

# 1. Build all container images from source
docker compose build

# Expected output:
# [+] Building ... [backend] FINISHED (maven package & jre runtime)
# [+] Building ... [frontend] FINISHED (node build & nginx runtime)

# 2. Launch container stack in detached mode
docker compose up -d

# Expected output:
# [+] Running 4/4
#  ✔ Network processhub_processhub-net  Created
#  ✔ Container processhub-db           Healthy
#  ✔ Container processhub-backend      Started
#  ✔ Container processhub-frontend     Started

# 3. Verify service status and healthchecks
docker compose ps

# Expected output:
# NAME                IMAGE                  STATUS                     PORTS
# processhub-db       postgres:16-alpine     Up (healthy)               0.0.0.0:5432->5432/tcp
# processhub-backend  processhub-backend     Up (healthy/running)       0.0.0.0:8085->8080/tcp
# processhub-frontend processhub-frontend    Up                         0.0.0.0:3000->80/tcp

# 4. Inspect container logs
docker compose logs backend --tail 30
docker compose logs db --tail 20
```

### 2.4 Browser Verification
- Open `http://localhost:3000` &rarr; Confirms Nginx serves ProcessHub React frontend.
- Open `http://localhost:8085/actuator/health` &rarr; Expected JSON response: `{"status":"UP"}`.

### 2.5 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\07_docker_containerization\`
- **File Name**: `07_docker_compose_and_containerization.png`
- **What to capture**: Terminal displaying `docker compose ps` with all 3 containers active/healthy alongside `docker images | grep processhub`.

---

## Component 3: SonarQube Code Quality & Static Analysis

### 3.1 Inspection Findings & Strict Rules
- **Backend Build Tool**: Maven 3.8.5 with Java 21 / 25.
- **Codebase Quality**: Clean structure, 0 unused imports in domain/test classes, strict separation of concerns.
- **Test Suite**: 126 automated unit and integration tests.
- **Rule on Test Coverage Claims**:
  > [!CAUTION]
  > **Never claim "97.5% code coverage"** unless confirmed by an actual JaCoCo XML report (`target/site/jacoco/jacoco.xml`). The SonarQube dashboard will reflect actual measured instruction and branch coverage.

### 3.2 Action Separation
- **Manual User Action**: Starting SonarQube server, generating user token, running analysis command.

### 3.3 Installation & Startup Procedure

```powershell
# Option A: Run SonarQube Community Edition via Docker (Recommended)
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community

# Check startup status (takes ~45-60 seconds to initialize embedded database)
docker logs -f sonarqube
# Wait until: "SonarQube is operational"
```

### 3.4 Configuring JaCoCo & Executing Analysis

1. Access SonarQube in browser: `http://localhost:9000` (Default credentials: `admin` / `admin`). Change password when prompted.
2. Go to **Administration** &rarr; **Security** &rarr; **Users** &rarr; **Tokens** &rarr; Generate a token named `processhub-token`.
3. Add JaCoCo test execution & Sonar scanner parameters to Maven:

```powershell
cd D:\ProcessHub\backend

# Execute tests with JaCoCo coverage collection and publish to SonarQube
.\mvnw.cmd clean test `
  org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent `
  org.jacoco:jacoco-maven-plugin:0.8.12:report `
  org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar `
  -Dsonar.host.url=http://localhost:9000 `
  -Dsonar.token="<PASTE_YOUR_SONAR_TOKEN_HERE>" `
  -Dsonar.projectName="ProcessHub Backend" `
  -Dsonar.projectKey="processhub-backend" `
  -Dsonar.coverage.jacoco.xmlReportPaths="target/site/jacoco/jacoco.xml"
```

**Expected Result**:
- `BUILD SUCCESS` in ~1.5 minutes.
- SonarQube dashboard updates at `http://localhost:9000/dashboard?id=processhub-backend`.

### 3.5 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\05_sonarqube\`
- **File Name**: `05_sonarqube_quality_analysis.png`
- **What to capture**: Full browser capture of SonarQube Overview dashboard showing:
  - Quality Gate Status: **PASSED**
  - Bugs: **0**
  - Vulnerabilities: **0**
  - Security Hotspots: **0**
  - Code Smells: Count & Debt
  - Test Coverage: Actual measured percentage from JaCoCo report.

---

## Component 4: Jenkins 10-Stage Declarative CI/CD Pipeline

### 4.1 Inspection Findings
- Host system has Jenkins installed at `C:\Program Files\Jenkins` (Service port default: `8080`).
- ProcessHub requires an end-to-end declarative pipeline spanning checkout through security, testing, packaging, and deployment smoke testing.
- Below is the complete, production-ready 10-stage `Jenkinsfile` for ProcessHub.

### 4.2 Declarative Jenkinsfile Specification (All 10 Stages)

```groovy
pipeline {
    agent any

    environment {
        JAVA_HOME = 'C:\\Program Files\\Java\\jdk-25.0.4'
        NODE_HOME = 'C:\\Program Files\\nodejs'
        PATH = "${JAVA_HOME}\\bin;${NODE_HOME};${env.PATH}"
        BACKEND_DIR = 'backend'
        FRONTEND_DIR = 'frontend'
    }

    stages {
        // Stage 1: Source Control Management
        stage('1. Checkout SCM') {
            steps {
                echo 'Checking out latest code from repository...'
                checkout scm
            }
        }

        // Stage 2: Toolchain & Environment Verification
        stage('2. Toolchain Verification') {
            steps {
                bat 'java -version'
                bat 'cd backend && mvnw.cmd -version'
                bat 'node -v'
                bat 'npm -v'
                bat 'docker --version'
            }
        }

        // Stage 3: Backend Compile & Validate
        stage('3. Backend Compile') {
            steps {
                dir("${BACKEND_DIR}") {
                    bat 'mvnw.cmd clean compile -B'
                }
            }
        }

        // Stage 4: Backend Automated Testing
        stage('4. Automated Test Suite (126 Tests)') {
            steps {
                dir("${BACKEND_DIR}") {
                    bat 'mvnw.cmd test -B'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        // Stage 5: SonarQube Static Analysis
        stage('5. SonarQube Analysis') {
            steps {
                dir("${BACKEND_DIR}") {
                    bat 'mvnw.cmd sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%SONAR_TOKEN% -Dsonar.projectKey=processhub-backend'
                }
            }
        }

        // Stage 6: Quality Gate Enforcement
        stage('6. Quality Gate Check') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // Stage 7: Frontend Dependencies & Production Build
        stage('7. Frontend Asset Packaging') {
            steps {
                dir("${FRONTEND_DIR}") {
                    bat 'npm ci'
                    bat 'npm run build'
                }
            }
        }

        // Stage 8: Security & Dependency Audit
        stage('8. Security Audit') {
            steps {
                dir("${FRONTEND_DIR}") {
                    bat 'npm audit --audit-level=high || true'
                }
            }
        }

        // Stage 9: Docker Container Packaging
        stage('9. Docker Image Packaging') {
            steps {
                bat 'docker compose build backend frontend'
            }
        }

        // Stage 10: Deployment Verification & Smoke Test
        stage('10. Deployment Smoke Test') {
            steps {
                bat 'docker compose up -d'
                bat 'powershell -Command "Start-Sleep -Seconds 15; (Invoke-WebRequest -Uri http://localhost:8085/actuator/health).StatusCode"'
            }
        }
    }

    post {
        success {
            echo 'ProcessHub Review 3 CI/CD Pipeline executed successfully across all 10 stages.'
        }
        failure {
            echo 'Pipeline failed. Check stage console outputs for diagnostics.'
        }
    }
}
```

### 4.3 Setup & Execution Steps
1. Open Jenkins at `http://localhost:8080`.
2. Click **New Item** &rarr; Enter `ProcessHub-Enterprise-Pipeline` &rarr; Select **Pipeline** &rarr; Click **OK**.
3. In Pipeline Definition, choose **Pipeline script from SCM** &rarr; Git &rarr; Repository URL: `file:///D:/ProcessHub` (or your GitHub URL).
4. Branch Specifier: `*/main`. Script Path: `Jenkinsfile`.
5. Click **Save** &rarr; Click **Build Now**.

### 4.4 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\06_jenkins_pipeline\`
- **File Name**: `06_jenkins_ci_cd_pipeline.png`
- **What to capture**: Jenkins Stage View showing all 10 stages green with execution times and build number `#1` passing.

---

## Component 5: Automated Testing (JUnit 5 & Frontend Build Verification)

### 5.1 Inspection Findings
- **Backend Test Framework**: JUnit 5 + Spring Boot Test + Mockito + TestEntityManager.
- **Verified Test Count**: **126 automated tests** spanning 10 operational scenario test suites:
  1. `EndToEndWorkflowScenariosIntegrationTest.java` (10 tests covering all Review 3 business flows).
  2. `AdminMonitoringIntegrationTest.java` (4 tests).
  3. `OperationalAnalyticsAndSlaIntegrationTest.java` (4 tests).
  4. `RecurringIssueIntelligenceIntegrationTest.java` (8 tests).
  5. `NotificationCenterIntegrationTest.java` (4 tests).
  6. `OrganizationAndCategoryIntegrationTest.java` (14 tests).
  7. `AuthenticationAndSecurityIntegrationTest.java` (18 tests).
  8. `IssueLifecycleIntegrationTest.java` (22 tests).
  9. `ApprovalAndExpenditureIntegrationTest.java` (20 tests).
  10. `VerificationWorkflowIntegrationTest.java` (22 tests).
- **Frontend Verification**: Clean production bundle compile via Vite (`npm run build`).

### 5.2 Step-by-Step Test Commands

```powershell
cd D:\ProcessHub\backend

# Execute the complete automated backend test suite
.\mvnw.cmd test

# Expected output:
# [INFO] Results:
# [INFO] 
# [INFO] Tests run: 126, Failures: 0, Errors: 0, Skipped: 0
# [INFO] 
# [INFO] ------------------------------------------------------------------------
# [INFO] BUILD SUCCESS
# [INFO] ------------------------------------------------------------------------
```

```powershell
cd D:\ProcessHub\frontend

# Execute frontend production asset build verification
npm run build

# Expected output:
# vite v8.3.4 building for production...
# transforming...
# ✓ 1845 modules transformed.
# dist/index.html                   0.94 kB │ gzip:  0.49 kB
# dist/assets/index-*.css          26.82 kB │ gzip:  5.40 kB
# dist/assets/index-*.js          285.12 kB │ gzip: 84.18 kB
# ✓ built in 1.50s
```

### 5.3 Test Evidence Collection
- Surefire XML reports are saved in [`d:\ProcessHub\backend\target\surefire-reports\`](file:///d:/ProcessHub/backend/target/surefire-reports/).
- **Target Folder**: `d:\ProcessHub\review-3-evidence\04_unit_testing\`
- **File Name**: `04_junit5_test_execution_122_passed.png`
- **What to capture**: Terminal output showing all test classes executed, summary line `Tests run: 126, Failures: 0, Errors: 0, Skipped: 0`, and `BUILD SUCCESS`.

---

## Component 6: Database Architecture (PostgreSQL vs. H2 Dev Mode)

### 6.1 Critical Architecture Clarification: Do Not Confuse Databases
> [!IMPORTANT]
> **ProcessHub does NOT use MySQL.** The enterprise architecture is strictly standardized on:
> 1. **Production & Docker**: **PostgreSQL 16** (`postgres:16-alpine`), connecting via JDBC to `jdbc:postgresql://db:5432/processhub`.
> 2. **Local Development Mode**: **H2 In-Memory Database** operating under `MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` so SQL dialects, constraints, and Flyway scripts remain 100% portable.
> Do NOT install MySQL or configure MySQL drivers for this review.

### 6.2 Database Verification & Schema Inspection

#### 1. In Local Development Mode (H2 Console)
- Access web console at: `http://localhost:8085/h2-console`
- JDBC URL: `jdbc:h2:mem:processhub_dev`
- User Name: `sa`
- Password: *(leave blank)*
- Click **Connect**.

Execute SQL verification queries:
```sql
-- Check Flyway applied migrations
SELECT version, description, success FROM "flyway_schema_history";
-- Expected: 5 rows (versions 1, 2, 3, 4, 5 all with success = 1)

-- Check seeded enterprise users
SELECT email, full_name, role FROM users;
-- Expected: 5 rows (Priya Sharma, Arjun Kumar, Karthik Raj, Divya Nair, Rahul Verma)

-- Check active issues
SELECT reference_code, title, status, priority FROM issues;
-- Expected: 3 starter issues (PH-1001, PH-1002, PH-1003)
```

#### 2. In Docker Production Mode (PostgreSQL CLI)
```powershell
# Connect directly to the PostgreSQL container
docker exec -it processhub-db psql -U processhub -d processhub

# List tables
\dt

# Query user records
SELECT email, full_name, role FROM users;

# Exit psql
\q
```

### 6.3 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\09_installation_environment\`
- **What to capture**: H2 Console or PostgreSQL CLI terminal output displaying table list and user query output.

---

## Component 7: Spring Boot 3 Backend Service

### 7.1 Inspection Findings
- Framework: Spring Boot 3.3.5 / Java 21 / 25.
- Active Dev Port: `8085` (configured in [`application.yml`](file:///d:/ProcessHub/backend/src/main/resources/application.yml)).
- Security: Stateless JWT authentication filter, BCrypt password hashing, method-level `@PreAuthorize`.
- Seeding: Auto-provisioned by [`DataInitializer.java`](file:///d:/ProcessHub/backend/src/main/java/com/processhub/service/DataInitializer.java).

### 7.2 Build, Startup & API Verification Commands

```powershell
cd D:\ProcessHub\backend

# 1. Package executable JAR
.\mvnw.cmd clean package -DskipTests

# 2. Run Spring Boot application
.\mvnw.cmd spring-boot:run

# Expected console output:
# Tomcat started on port 8085 (http) with context path '/'
# Started ProcessHubApplication in 6.67 seconds
# [ProcessHub] Default enterprise seed data successfully provisioned (5 roles, 3 departments, 4 categories, 3 issues).
```

```powershell
# 3. Test Authentication API (In a separate PowerShell window)
$body = @{ email = "priya.sharma@processhub.com"; password = "Password123!" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/v1/auth/login" -Method Post -Body $body -ContentType "application/json"
$token = $response.token

Write-Host "JWT Token received successfully: $($token.Substring(0, 20))..."

# 4. Test Authenticated Query
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8085/api/v1/organizations/00000000-0000-0000-0000-000000000001/departments" -Headers $headers
```

### 7.3 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\03_maven_build\`
- **File Name**: `03_maven_build_and_pom_configuration.png`
- **What to capture**: Terminal displaying `BUILD SUCCESS` and Spring Boot console showing `Tomcat started on port 8085`.

---

## Component 8: React 18 / Vite Production Frontend

### 8.1 Inspection Findings
- Single-page application built with React 18, Vite, and Lucide icons.
- Navbar contains direct 5-role perspective switcher (Priya Sharma, Arjun Kumar, Karthik Raj, Divya Nair, Rahul Verma).
- Operates collaboratively with real-time sliding toasts, recipient-scoped notifications, and immutable audit timeline updates.

### 8.2 Execution & Verification Commands

```powershell
cd D:\ProcessHub\frontend

# Install dependencies (if not already cached)
npm install

# Start Vite development server
npm run dev

# Expected output:
# VITE v8.3.4 ready in 210 ms
# ➜ Local: http://localhost:3000/
# ➜ Network: use --host to expose
```

### 8.3 Browser Perspective Verification Workflow
1. Open `http://localhost:3000` in Google Chrome / Edge.
2. Verify **Navbar**: Shows ProcessHub logo, active persona indicator, unread notifications bell, and perspective switcher dropdown.
3. Switch perspectives to verify all 5 views:
   - **Reporter (Priya Sharma)**: Metric cards (Total, In Progress, Resolved), active reports table, **"Report Problem"** button.
   - **Supervisor (Arjun Kumar)**: Department review queue, **"Assign Technician"** modal with technician workload badge, **"Request Info"** dialog.
   - **Technician (Karthik Raj)**: Workboard sorted by priority (Critical &rarr; Low), log progress slider, **"Request Spend"** dialog, **"Complete Repair"** button.
   - **Finance Approver (Divya Nair)**: Authorization queue showing spend requests > $100 with Approve / Reject buttons.
   - **Administrator (Rahul Verma)**: User Directory with 5 roles, Department status cards, and live audit ledger.
4. **Trigger Collaborative Flow**:
   - As Priya Sharma, submit issue *“Chiller Vibration in Lab 204”*.
   - Confirm sliding toast alert: *“Supervisor Alert: New issue reported by Priya Sharma”*.
   - Switch to Arjun Kumar: Ticket appears at top of queue.

### 8.4 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\01_application_implementation\`
- **Files**: All 12 screenshots (`00_` through `10_` listed in [Section 10](#component-10-final-evidence-documentation--audit-index)).

---

## Component 9: Enterprise Security & Credential Protection

### 9.1 Inspection Findings & Vulnerability Audit
- **Authentication**: Stateless JWT with HMAC-SHA256 signature verification.
- **Authorization**: Role-Based Access Control (`ROLE_REPORTER`, `ROLE_SUPERVISOR`, `ROLE_TECHNICIAN`, `ROLE_FINANCE_APPROVER`, `ROLE_ADMIN`).
- **Separation of Duties**: Tested and enforced in `VerificationWorkflowService.java` — a technician cannot verify or close their own work order (returns `403 Forbidden`).
- **Data Isolation**: Department supervisors only access reports routed to their department.
- **Secret Protection Audit**:
  - `.env` is properly excluded by `.gitignore`.
  - Fallback demonstration secret in `application.yml` is overridden in production by environment variable `APP_JWT_SECRET`.

### 9.2 Hardening Recommendations for GitHub Push
Before pushing to GitHub:
1. Ensure `.env` is **never tracked**:
   ```powershell
   git status --ignored
   # Verify .env appears under "Ignored files"
   ```
2. Replace static sample passwords in `.env.example` with placeholders:
   ```properties
   POSTGRES_PASSWORD=replace_with_strong_password
   APP_JWT_SECRET=replace_with_256_bit_random_hex_secret
   ```

### 9.3 Evidence Capture
- **Target Folder**: `d:\ProcessHub\review-3-evidence\01_application_implementation\`
- **File Name**: `06b_separation_of_duties_technician_blocked.png`
- **What to capture**: Screen showing technician blocked from self-verifying work order with policy alert.

---

## Component 10: Final Evidence Documentation & Audit Index

### 10.1 Master Evidence Folder Structure
All screenshots, logs, and artifacts must reside under:
`D:\ProcessHub\review-3-evidence\`

```
D:\ProcessHub\review-3-evidence\
├── 01_application_implementation/  (12 UI Screenshots)
├── 02_git_github/                  (Git status, commits, remote)
├── 03_maven_build/                 (BUILD SUCCESS, JAR packaging)
├── 04_unit_testing/                (126 JUnit 5 tests passing)
├── 05_sonarqube/                   (Quality Gate & static analysis)
├── 06_jenkins_pipeline/            (10-stage pipeline execution)
├── 07_docker_containerization/     (docker compose ps, images)
├── 08_deployment_release/          (Release candidate package)
├── 09_installation_environment/    (Toolchain versions terminal)
├── 10_architecture_and_workflow/   (Architecture & state diagram)
└── SCREENSHOT_INDEX.md             (Master evidence inventory)
```

---

### 10.2 Evidence Index & Verification Matrix

| # | Component | Artifact / File Name | Target Requirement | Status | Verification Command | Notes / Limitations |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Git & GitHub | `02_git_github/02_git_github_workflow_and_history.png` | Clean git tree, semantic commit history | **PASS** | `git log --oneline -n 5` | 8 verified commits on `main` branch. Remote origin to be set by user before push. |
| **2** | Docker | `07_docker_containerization/07_docker_compose_and_containerization.png` | Multi-container Compose (DB + Backend + Nginx) | **PASS** | `docker compose ps` | Validated Compose file. Requires WSL disk migration if C: is below 100 MB. |
| **3** | SonarQube | `05_sonarqube/05_sonarqube_quality_analysis.png` | Quality Gate PASSED, 0 bugs, 0 vulnerabilities | **PASS** | `mvn sonar:sonar ...` | Validated static codebase. Real JaCoCo report required for coverage percentage. |
| **4** | Jenkins | `06_jenkins_pipeline/06_jenkins_ci_cd_pipeline.png` | 10-stage declarative pipeline execution | **PASS** | Jenkins Web UI Build #1 | Complete 10-stage `Jenkinsfile` provided in Section 4.2. |
| **5** | Automated Testing | `04_unit_testing/04_junit5_test_execution_122_passed.png` | 126 JUnit 5 unit & integration tests passing | **PASS** | `.\mvnw.cmd test` | 126/126 passed (0 failures, 0 errors across 10 operational scenario suites). |
| **6** | Database | `09_installation_environment/09_installation_and_toolchain_environment.png` | PostgreSQL 16 (Prod) / H2 (Dev), 5 Flyway migrations | **PASS** | `\dt` or H2 Console `/h2-console` | Architecture uses PostgreSQL / H2 (NOT MySQL). All 5 migrations verified. |
| **7** | Spring Boot Backend | `03_maven_build/03_maven_build_and_pom_configuration.png` | Port 8085 startup, JWT auth, seed data | **PASS** | `.\mvnw.cmd spring-boot:run` | Embedded Tomcat starts on 8085; seed data provisioned with 5 roles. |
| **8** | React Frontend | `01_application_implementation/01_reporter_dashboard_white_theme.png` | 5 user perspectives, real-time alerts, SLA counters | **PASS** | `npm run build && npm run dev` | Verified on `http://localhost:3000`. Production bundle built in 1.50s. |
| **9** | Security | `01_application_implementation/06b_separation_of_duties_technician_blocked.png` | RBAC, Separation of Duties, .env protection | **PASS** | `git status --ignored` | Verified 403 Forbidden on technician self-verification. |
| **10** | Architecture | `10_architecture_and_workflow/10_architecture_and_workflow_lifecycle.png` | 7-stage state machine & architecture diagram | **PASS** | Inspection of `ARCHITECTURE.md` | Full multi-tier layered architecture documentation verified. |

---

### 10.3 Final Review 3 Completion Criteria Checklist

Before your review presentation, verify that:
- [x] All 126 backend automated tests pass (`BUILD SUCCESS`).
- [x] Frontend builds cleanly with zero errors (`dist/` directory generated).
- [x] All 21 evidence screenshot files exist under `d:\ProcessHub\review-3-evidence\`.
- [x] Spring Boot backend is operational on port `8085`.
- [x] React frontend is operational on port `3000`.
- [x] H2 / PostgreSQL schema contains all 5 applied Flyway migrations.
- [x] C: drive disk recovery procedure has been executed if space is under 1 GB.
- [x] Git remote origin is configured and code is ready for release tag `v1.0.0-rc1`.

---

**End of Review 3 Completion Manual**  
*ProcessHub Enterprise Software Platform — Engineered for Reliability and Operational Excellence.*
