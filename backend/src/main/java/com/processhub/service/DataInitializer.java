package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.NotificationType;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    public static final UUID DEFAULT_ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final IssueCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            IssueCategoryRepository categoryRepository,
            UserRepository userRepository,
            OrganizationMembershipRepository membershipRepository,
            IssueRepository issueRepository,
            IssueTimelineEventRepository timelineRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail("priya.sharma@processhub.com").isPresent()) {
            log.info("[ProcessHub] Seed data already initialized. Skipping.");
            return;
        }

        log.info("[ProcessHub] Initializing default enterprise organization, roles, and users...");

        // 1. Organization
        Organization org = organizationRepository.findById(DEFAULT_ORG_ID)
                .orElseGet(() -> organizationRepository.save(
                        new Organization(DEFAULT_ORG_ID, "ProcessHub Enterprise HQ", "HQ")
                ));

        // 2. Departments
        Department facilitiesDept = departmentRepository.save(new Department(
                UUID.fromString("22222222-2222-2222-2222-222222222201"),
                org,
                "Facilities & Maintenance",
                "FAC",
                "Civil infrastructure, HVAC, plumbing, structural repairs"
        ));

        Department itDept = departmentRepository.save(new Department(
                UUID.fromString("22222222-2222-2222-2222-222222222202"),
                org,
                "IT Services",
                "IT",
                "Enterprise computing, network operations, hardware support"
        ));

        Department elecDept = departmentRepository.save(new Department(
                UUID.fromString("22222222-2222-2222-2222-222222222203"),
                org,
                "Electrical Systems",
                "ELEC",
                "High-voltage distribution, panels, backup generators"
        ));

        // 3. Issue Categories
        IssueCategory plumbingCat = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), org, facilitiesDept, "PLUMBING", "Plumbing & Water Systems",
                "Water supply, drainage, pipe leakages, fixtures", "PIPE_BURST,LEAK,FIXTURE", "HIGH", false
        ));

        categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), org, facilitiesDept, "HVAC", "HVAC & Climate Control",
                "Air conditioning, heating, ventilation, chiller units", "CHILLER,AIR_HANDLER,THERMOSTAT", "CRITICAL", true
        ));

        IssueCategory networkCat = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), org, itDept, "NETWORK", "Core Network Outage",
                "Switch failure, router dropouts, fiber connectivity", "SWITCH,ROUTER,WIFI_AP", "CRITICAL", true
        ));

        categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), org, elecDept, "POWER", "Circuit Breaker & Power",
                "Tripped breakers, power loss, UPS failure", "BREAKER,TRANSFORMER,OUTLET", "HIGH", true
        ));

        // 4. Default 5 Users (Password: Password123!)
        String encodedPassword = passwordEncoder.encode("Password123!");

        // Role 1: Reporter (Priya Sharma)
        User priya = userRepository.save(new User(
                UUID.randomUUID(),
                "priya.sharma@processhub.com",
                encodedPassword,
                "Priya Sharma",
                "+1-555-0101",
                Role.ROLE_REPORTER,
                "Research Operations"
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), org, priya, facilitiesDept, Role.ROLE_REPORTER
        ));

        // Role 2: Supervisor (Arjun Kumar)
        User arjun = userRepository.save(new User(
                UUID.randomUUID(),
                "arjun.kumar@processhub.com",
                encodedPassword,
                "Arjun Kumar",
                "+1-555-0102",
                Role.ROLE_SUPERVISOR,
                "Facilities & Maintenance"
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), org, arjun, facilitiesDept, Role.ROLE_SUPERVISOR
        ));

        // Role 3: Technician (Karthik Raj)
        User karthik = userRepository.save(new User(
                UUID.randomUUID(),
                "karthik.raj@processhub.com",
                encodedPassword,
                "Karthik Raj",
                "+1-555-0103",
                Role.ROLE_TECHNICIAN,
                "Facilities & Maintenance"
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), org, karthik, facilitiesDept, Role.ROLE_TECHNICIAN
        ));

        // Role 4: Finance Approver (Divya Nair)
        User divya = userRepository.save(new User(
                UUID.randomUUID(),
                "divya.nair@processhub.com",
                encodedPassword,
                "Divya Nair",
                "+1-555-0104",
                Role.ROLE_FINANCE_APPROVER,
                "Finance Administration"
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), org, divya, null, Role.ROLE_FINANCE_APPROVER
        ));

        // Role 5: Administrator (Rahul Verma)
        User rahul = userRepository.save(new User(
                UUID.randomUUID(),
                "rahul.verma@processhub.com",
                encodedPassword,
                "Rahul Verma",
                "+1-555-0105",
                Role.ROLE_ADMIN,
                "Executive Administration"
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), org, rahul, null, Role.ROLE_ADMIN
        ));

        // 5. Initial Shared Issues for Demonstrating Workflow
        // Issue 1: Under Review (Reporter submitted -> Supervisor reviewing)
        Issue issue1 = new Issue(
                UUID.randomUUID(),
                "PH-1001",
                org,
                facilitiesDept,
                plumbingCat,
                priya,
                "PIPE_BURST",
                "Ceiling Water Leak above Lab Bench 4",
                "Continuous dripping from overhead pipe onto research workstation. Potential damage to optical equipment.",
                "Science Complex A",
                "Lab 102",
                "PIPE-SCI-102",
                Priority.HIGH
        );
        issue1.setStatus(IssueStatus.UNDER_REVIEW);
        issue1.setCurrentStage("UNDER_SUPERVISOR_REVIEW");
        issue1.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
        issue1.setNextAction("Supervisor (Arjun Kumar) must review report and assign technician");
        issue1.setSlaDueAt(Instant.now().plus(12, ChronoUnit.HOURS));
        issue1 = issueRepository.save(issue1);

        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue1, priya, TimelineAction.ISSUE_SUBMITTED.name(),
                null, IssueStatus.SUBMITTED.name(), "Problem report submitted: " + issue1.getTitle()
        ));
        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue1, arjun, TimelineAction.REVIEW_STARTED.name(),
                IssueStatus.SUBMITTED.name(), IssueStatus.UNDER_REVIEW.name(), "Supervisor triage initiated by Arjun Kumar"
        ));

        // Issue 2: Assigned & In Progress (Technician working on repair)
        Issue issue2 = new Issue(
                UUID.randomUUID(),
                "PH-1002",
                org,
                facilitiesDept,
                plumbingCat,
                priya,
                "PUMP",
                "Commercial Sump Pump Motor Burnout",
                "Sump basin in utility sub-basement failing to cycle. Motor windings burned out from heavy sediment load.",
                "Building B (Basement)",
                "Pump Vault 003",
                "PUMP-B-003",
                Priority.CRITICAL
        );
        issue2.setStatus(IssueStatus.IN_PROGRESS);
        issue2.setAssignedTechnician(karthik);
        issue2.setCurrentStage("IN_PROGRESS");
        issue2.setResponsibleRole(Role.ROLE_TECHNICIAN.name());
        issue2.setNextAction("Technician (Karthik Raj) diagnosing pump assembly; replacement impeller required");
        issue2.setSlaDueAt(Instant.now().plus(4, ChronoUnit.HOURS));
        issue2 = issueRepository.save(issue2);

        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue2, priya, TimelineAction.ISSUE_SUBMITTED.name(),
                null, IssueStatus.SUBMITTED.name(), "Problem report submitted: " + issue2.getTitle()
        ));
        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue2, arjun, TimelineAction.TECHNICIAN_ASSIGNED.name(),
                IssueStatus.SUBMITTED.name(), IssueStatus.ASSIGNED.name(), "Assigned to Karthik Raj with standard dispatch instructions"
        ));
        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue2, karthik, TimelineAction.STATUS_CHANGED.name(),
                IssueStatus.ASSIGNED.name(), IssueStatus.IN_PROGRESS.name(), "Work started by assigned technician: Karthik Raj"
        ));

        // Issue 3: Pending Verification (Technician completed work -> Supervisor sign-off needed)
        Issue issue3 = new Issue(
                UUID.randomUUID(),
                "PH-1003",
                org,
                itDept,
                networkCat,
                priya,
                "SWITCH",
                "Floor 3 Access Switch Port Flapping",
                "Multiple switch ports resetting every 15 minutes causing workstation disconnects.",
                "Tower West",
                "IDF 304",
                "SW-TW-304",
                Priority.HIGH
        );
        issue3.setStatus(IssueStatus.RESOLVED_PENDING_VERIFICATION);
        issue3.setAssignedTechnician(karthik);
        issue3.setCurrentStage("RESOLVED_PENDING_VERIFICATION");
        issue3.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
        issue3.setBlockingReason("Pending independent verification and sign-off");
        issue3.setNextAction("Supervisor or Reporter quality and completion sign-off");
        issue3.setWorkSummary("Replaced faulty SFP transceiver on uplink port 24 and reseated optic patch fiber.");
        issue3.setRootCause("Overheated optical transceiver module failing under peak packet load.");
        issue3.setLaborHours(java.math.BigDecimal.valueOf(1.5));
        issue3.setResolvedAt(Instant.now().minus(30, ChronoUnit.MINUTES));
        issue3.setSlaDueAt(Instant.now().plus(8, ChronoUnit.HOURS));
        issue3 = issueRepository.save(issue3);

        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue3, priya, TimelineAction.ISSUE_SUBMITTED.name(),
                null, IssueStatus.SUBMITTED.name(), "Problem report submitted: " + issue3.getTitle()
        ));
        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue3, arjun, TimelineAction.TECHNICIAN_ASSIGNED.name(),
                IssueStatus.SUBMITTED.name(), IssueStatus.ASSIGNED.name(), "Assigned to Karthik Raj"
        ));
        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue3, karthik, TimelineAction.STATUS_CHANGED.name(),
                IssueStatus.ASSIGNED.name(), IssueStatus.IN_PROGRESS.name(), "Work started"
        ));
        timelineRepository.save(new IssueTimelineEvent(
                UUID.randomUUID(), issue3, karthik, TimelineAction.SUBMITTED_FOR_VERIFICATION.name(),
                IssueStatus.IN_PROGRESS.name(), IssueStatus.RESOLVED_PENDING_VERIFICATION.name(),
                "Resolution submitted: Replaced faulty SFP transceiver on uplink port 24"
        ));

        // Initial recipient-specific notifications
        // Notification for Arjun (Supervisor): Issue 1 needs review
        notificationRepository.save(new Notification(
                UUID.randomUUID(), org, arjun, issue1,
                NotificationType.ISSUE_SUBMITTED,
                "New Issue: PH-1001",
                "New problem report submitted in Facilities & Maintenance: Ceiling Water Leak above Lab Bench 4"
        ));

        // Notification for Arjun (Supervisor): Issue 3 ready for verification
        notificationRepository.save(new Notification(
                UUID.randomUUID(), org, arjun, issue3,
                NotificationType.RESOLUTION_SUBMITTED,
                "Resolution Pending Verification: PH-1003",
                "Technician Karthik Raj completed work on PH-1003. Ready for independent verification."
        ));

        // Notification for Karthik (Technician): Assigned to Issue 2
        notificationRepository.save(new Notification(
                UUID.randomUUID(), org, karthik, issue2,
                NotificationType.ISSUE_ASSIGNED,
                "Work Order Assigned: PH-1002",
                "You have been assigned to: Commercial Sump Pump Motor Burnout"
        ));

        // Notification for Priya (Reporter): Submission confirmation for PH-1001
        notificationRepository.save(new Notification(
                UUID.randomUUID(), org, priya, issue1,
                NotificationType.ISSUE_SUBMITTED,
                "Submission Confirmation: PH-1001",
                "Your problem report 'Ceiling Water Leak above Lab Bench 4' has been submitted successfully."
        ));

        // Notification for Priya (Reporter): Repair completed on PH-1003
        notificationRepository.save(new Notification(
                UUID.randomUUID(), org, priya, issue3,
                NotificationType.RESOLUTION_SUBMITTED,
                "Repair Completed: PH-1003",
                "Repair work on your problem report 'Floor 3 Access Switch Port Flapping' is completed and awaiting quality verification."
        ));

        log.info("[ProcessHub] Default enterprise seed data successfully provisioned (5 roles, 3 departments, 4 categories, 3 issues).");
    }
}
