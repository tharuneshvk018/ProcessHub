# Review 3 Screenshot Evidence Index — ProcessHub Enterprise

This index catalogs genuine evidence screenshots collected from the ProcessHub codebase, runtime services, test runners, build pipelines, and application interfaces for Review 3 of **Application Development and Deployment Architecture (ADDA)**.

---

## 1. Directory Structure

```
D:\ProcessHub\review-3-evidence\
├── 01_application_implementation/
│   ├── 00_enterprise_login_page_white_theme.png
│   ├── 01_reporter_dashboard_white_theme.png
│   ├── 02_problem_reporting_modal.png
│   ├── 03_supervisor_review_queue.png
│   ├── 04_technician_workboard.png
│   ├── 05_finance_approval_queue.png
│   ├── 06_verification_and_separation_of_duties.png
│   ├── 06b_separation_of_duties_technician_blocked.png
│   ├── 07_operational_analytics_and_sla.png
│   ├── 08_issue_detail_explainable_progress_recurrence.png
│   ├── 09_financial_expenditure_blocking_detail.png
│   └── 10_responsive_mobile_layout.png
├── 02_git_github/
│   └── 02_git_github_workflow_and_history.png
├── 03_maven_build/
│   └── 03_maven_build_and_pom_configuration.png
├── 04_unit_testing/
│   └── 04_junit5_test_execution_122_passed.png
├── 05_sonarqube/
│   └── 05_sonarqube_quality_analysis.png
├── 06_jenkins_pipeline/
│   └── 06_jenkins_ci_cd_pipeline.png
├── 07_docker_containerization/
│   └── 07_docker_compose_and_containerization.png
├── 08_deployment_release/
│   └── 08_deployment_and_release_candidate.png
├── 09_installation_environment/
│   └── 09_installation_and_toolchain_environment.png
└── 10_architecture_and_workflow/
    └── 10_architecture_and_workflow_lifecycle.png
```

---

## 2. Screenshot Inventory & Requirement Mapping

| # | Screenshot Filename | What It Demonstrates | Review 3 Requirement | Evidence Type | Notes / Limitations |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `01_application_implementation/00_enterprise_login_page_white_theme.png` | Enterprise IAM login card with white theme, Priya Sharma credentials, and stateless JWT badge | Application Implementation | Implementation Evidence | Captures simulated SSO form and persona picker |
| **2** | `01_application_implementation/01_reporter_dashboard_white_theme.png` | Main Reporter portal in white theme with metrics, active tickets (PH-1001 to PH-1005), and SLA tags | Application Implementation | Implementation Evidence | Proves clean light theme across dashboards and tables |
| **3** | `01_application_implementation/02_problem_reporting_modal.png` | Problem submission dialog with department routing, asset tag requirement, and priority selection | Application Implementation | Implementation Evidence | Dynamic validation prevents duplicate/empty issues |
| **4** | `01_application_implementation/03_supervisor_review_queue.png` | Supervisor triage workboard for Arjun Kumar with Assign, RFI, and Reject decision actions | Application Implementation | Implementation Evidence | Proves department supervisor queue isolation |
| **5** | `01_application_implementation/04_technician_workboard.png` | Workboard for technician Karthik Raj with active tasks, progress logging, and spend requests | Application Implementation | Implementation Evidence | Prioritized queue by severity (Critical &rarr; High) |
| **6** | `01_application_implementation/05_finance_approval_queue.png` | Finance authorization queue for Divya Nair approving spend requests exceeding $100 | Application Implementation | Implementation Evidence | Shows $380 pump motor request unblocking repair |
| **7** | `01_application_implementation/06_verification_and_separation_of_duties.png` | Independent sign-off view authorized for supervisor/reporter to inspect technician work | Application Implementation | Implementation Evidence | Enforces separation of duties policy |
| **8** | `01_application_implementation/06b_separation_of_duties_technician_blocked.png` | Technician view showing self-verification strictly blocked with policy alert | Application Implementation | Implementation Evidence | Proves technician cannot sign off their own work |
| **9** | `01_application_implementation/07_operational_analytics_and_sla.png` | SLA compliance meter, MTTR metrics, and manual SLA timer breach scanner | Application Implementation | Implementation Evidence | Demonstrates analytics and escalation trigger |
| **10** | `01_application_implementation/08_issue_detail_explainable_progress_recurrence.png` | Deep inspection drawer with Chronic Repeat alert, rolling 90-day history, and audit timeline | Application Implementation | Implementation Evidence | Demonstrates explainable progress and intelligence |
| **11** | `01_application_implementation/09_financial_expenditure_blocking_detail.png` | Explainable progress banner with active blocker explanation for unapproved spend | Application Implementation | Implementation Evidence | Shows transparent blocker reasoning to users |
| **12** | `01_application_implementation/10_responsive_mobile_layout.png` | Mobile responsive layout on 420px viewport demonstrating adaptive cards and navigation | Application Implementation | Implementation Evidence | Verifies responsiveness across mobile screens |
| **13** | `02_git_github/02_git_github_workflow_and_history.png` | Git status, main branch, release tags, and semantic commit history | Git & GitHub | Configuration / Workflow Evidence | Local repository verified; remote origin configured |
| **14** | `03_maven_build/03_maven_build_and_pom_configuration.png` | Maven compile/package execution, Spring Boot 3.3.5 pom.xml, and BUILD SUCCESS | Maven Build | Configuration / Build Evidence | Executable JAR generated in `backend/target` |
| **15** | `04_unit_testing/04_junit5_test_execution_122_passed.png` | Real test execution of 122 automated JUnit 5 tests passing with 0 failures and 0 errors | Unit Testing | Execution Evidence | All 10 mandatory operational scenarios verified |
| **16** | `05_sonarqube/05_sonarqube_quality_analysis.png` | Static code analysis report, quality gate status (PASSED), 0 bugs, 0 vulnerabilities | Code Quality (SonarQube) | Quality Analysis Evidence | Audit confirms zero hardcoded secrets & clean code |
| **17** | `06_jenkins_pipeline/06_jenkins_ci_cd_pipeline.png` | Jenkins running service (Port 8080) and 6-stage CI/CD declarative pipeline execution | Jenkins Pipeline | CI/CD Evidence | Service verified active on localhost:8080 |
| **18** | `07_docker_containerization/07_docker_compose_and_containerization.png` | Docker Compose config, multi-stage backend/frontend Dockerfiles, and image build output | Docker Containerization | Containerization Evidence | Validates PostgreSQL + Backend + Nginx stack |
| **19** | `08_deployment_release/08_deployment_and_release_candidate.png` | Release Candidate 1 checklist, production bundle sizes, and environment verification | Deployment & Release | Release Verification Evidence | Proves production bundle readiness |
| **20** | `09_installation_environment/09_installation_and_toolchain_environment.png` | Unified toolchain terminal showing Java 25 LTS, Maven 3.8.5, Node 22, Git, Docker, and Jenkins | Installation & Environment | Environment Evidence | Consolidated evidence of all required tools |
| **21** | `10_architecture_and_workflow/10_architecture_and_workflow_lifecycle.png` | Multi-tier system architecture diagram and 7-stage issue state machine lifecycle | Architecture & Workflow | Architecture Evidence | Reflects actual codebase implementation |
