package com.processhub.service;

import com.processhub.domain.entity.Organization;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.CreateOrganizationRequest;
import com.processhub.dto.OrganizationResponse;
import com.processhub.exception.DuplicateResourceException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        String normalizedCode = request.getCode().trim().toUpperCase();

        if (organizationRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("Organization with code '" + normalizedCode + "' already exists");
        }

        Organization organization = new Organization(
                UUID.randomUUID(),
                request.getName().trim(),
                normalizedCode
        );

        Organization saved = organizationRepository.save(organization);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganization(UUID orgId, String userEmail) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        validateUserOrganizationAccess(orgId, userEmail);

        return mapToResponse(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> listOrganizationsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return organizationRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return membershipRepository.findByUserIdAndActiveTrue(user.getId()).stream()
                .map(m -> mapToResponse(m.getOrganization()))
                .distinct()
                .collect(Collectors.toList());
    }

    public void validateUserOrganizationAccess(UUID orgId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Global admin has system-wide access
        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        boolean hasAccess = membershipRepository.existsByOrganizationIdAndUserIdAndActiveTrue(orgId, user.getId());
        if (!hasAccess) {
            throw new AccessDeniedException("Access denied: You are not an authorized member of organization " + orgId);
        }
    }

    public void validateUserOrganizationAdmin(UUID orgId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Global admin has system-wide administrative access
        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        boolean isOrgAdmin = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                orgId, user.getId(), Role.ROLE_ADMIN);

        if (!isOrgAdmin) {
            throw new AccessDeniedException("Access denied: Administrative privileges required for organization " + orgId);
        }
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getCode(),
                org.getStatus(),
                org.getCreatedAt()
        );
    }
}
