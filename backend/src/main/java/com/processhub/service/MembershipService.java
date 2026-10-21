package com.processhub.service;

import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.entity.OrganizationMembership;
import com.processhub.domain.entity.User;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.CreateMembershipRequest;
import com.processhub.dto.MembershipResponse;
import com.processhub.exception.DuplicateResourceException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;

    public MembershipService(
            OrganizationMembershipRepository membershipRepository,
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            OrganizationService organizationService) {
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
    }

    @Transactional
    public MembershipResponse assignMembership(UUID orgId, CreateMembershipRequest request, String currentUserEmail) {
        // Enforce administrative privileges within target organization
        organizationService.validateUserOrganizationAdmin(orgId, currentUserEmail);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

            if (!department.getOrganization().getId().equals(orgId)) {
                throw new IllegalArgumentException("Department does not belong to organization " + orgId);
            }
        }

        // Check if exact membership already exists
        boolean exists = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                orgId, user.getId(), request.getRole());
        if (exists && request.getDepartmentId() == null) {
            throw new DuplicateResourceException("User already has role " + request.getRole() + " in organization " + organization.getName());
        }

        OrganizationMembership membership = new OrganizationMembership(
                UUID.randomUUID(),
                organization,
                user,
                department,
                request.getRole()
        );

        OrganizationMembership saved = membershipRepository.save(membership);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getMembershipsForOrganization(UUID orgId, String currentUserEmail) {
        // Enforce organization member access
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        return membershipRepository.findByOrganizationIdAndActiveTrue(orgId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getUserMemberships(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        return membershipRepository.findByUserIdAndActiveTrue(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MembershipResponse mapToResponse(OrganizationMembership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getOrganization().getId(),
                membership.getOrganization().getName(),
                membership.getUser().getId(),
                membership.getUser().getEmail(),
                membership.getUser().getFullName(),
                membership.getDepartment() != null ? membership.getDepartment().getId() : null,
                membership.getDepartment() != null ? membership.getDepartment().getName() : null,
                membership.getRole(),
                membership.isActive(),
                membership.getCreatedAt()
        );
    }
}
