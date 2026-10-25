package com.processhub.service;

import com.processhub.domain.entity.Issue;
import com.processhub.domain.entity.OrganizationMembership;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.dto.RoutingDecision;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowRoutingService {

    private final OrganizationMembershipRepository membershipRepository;

    public WorkflowRoutingService(OrganizationMembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public RoutingDecision evaluateRouting(Issue issue) {
        List<OrganizationMembership> supervisors = membershipRepository
                .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(
                        issue.getOrganization().getId(),
                        issue.getDepartment().getId(),
                        Role.ROLE_SUPERVISOR);

        // Exception Handling: No active supervisor registered for target department
        if (supervisors.isEmpty()) {
            return new RoutingDecision(
                    "UNASSIGNED_TRIAGE",
                    Role.ROLE_ADMIN.name(),
                    "Routing Exception: No active supervisor configured for department " + issue.getDepartment().getName(),
                    "Organization Administrator must assign a department supervisor or manually triage this report",
                    true,
                    null
            );
        }

        User primarySupervisor = supervisors.get(0).getUser();

        // Multi-Factor Context Evaluation: Priority / Safety urgency
        if (issue.getPriority() == Priority.CRITICAL) {
            return new RoutingDecision(
                    "UNDER_SUPERVISOR_REVIEW",
                    Role.ROLE_SUPERVISOR.name(),
                    "Critical Priority: Immediate supervisor triage and technician deployment required",
                    "Supervisor must expedite triage and assign emergency technician immediately",
                    false,
                    primarySupervisor
            );
        }

        return new RoutingDecision(
                "UNDER_SUPERVISOR_REVIEW",
                Role.ROLE_SUPERVISOR.name(),
                null,
                "Supervisor will review report, verify severity, and assign an eligible technician",
                false,
                primarySupervisor
        );
    }
}
