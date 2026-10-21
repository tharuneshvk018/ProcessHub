package com.processhub.service;

import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.dto.CreateDepartmentRequest;
import com.processhub.dto.DepartmentResponse;
import com.processhub.exception.DuplicateResourceException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            OrganizationRepository organizationRepository,
            OrganizationService organizationService) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.organizationService = organizationService;
    }

    @Transactional
    public DepartmentResponse createDepartment(UUID orgId, CreateDepartmentRequest request, String currentUserEmail) {
        // Enforce administrative access for this organization
        organizationService.validateUserOrganizationAdmin(orgId, currentUserEmail);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        String normalizedCode = request.getCode().trim().toUpperCase();

        if (departmentRepository.existsByOrganizationIdAndCode(orgId, normalizedCode)) {
            throw new DuplicateResourceException(
                    "Department with code '" + normalizedCode + "' already exists in organization " + organization.getName());
        }

        Department department = new Department(
                UUID.randomUUID(),
                organization,
                request.getName().trim(),
                normalizedCode,
                request.getDescription() != null ? request.getDescription().trim() : null
        );

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsForOrganization(UUID orgId, String currentUserEmail) {
        // Enforce organization data isolation
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        return departmentRepository.findByOrganizationIdAndActiveTrue(orgId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(UUID orgId, UUID departmentId, String currentUserEmail) {
        // Enforce organization data isolation
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        if (!department.getOrganization().getId().equals(orgId)) {
            throw new ResourceNotFoundException("Department " + departmentId + " does not belong to organization " + orgId);
        }

        return mapToResponse(department);
    }

    private DepartmentResponse mapToResponse(Department dept) {
        return new DepartmentResponse(
                dept.getId(),
                dept.getOrganization().getId(),
                dept.getName(),
                dept.getCode(),
                dept.getDescription(),
                dept.isActive(),
                dept.getCreatedAt()
        );
    }
}
