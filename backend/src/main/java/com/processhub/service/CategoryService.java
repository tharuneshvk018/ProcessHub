package com.processhub.service;

import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.IssueCategory;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.IssueCategoryRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.dto.CategoryResponse;
import com.processhub.dto.CreateCategoryRequest;
import com.processhub.exception.DuplicateResourceException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final IssueCategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationService organizationService;

    public CategoryService(
            IssueCategoryRepository categoryRepository,
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            OrganizationService organizationService) {
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.organizationService = organizationService;
    }

    @Transactional
    public CategoryResponse createCategory(UUID orgId, CreateCategoryRequest request, String currentUserEmail) {
        // Enforce organization admin access for category configuration
        organizationService.validateUserOrganizationAdmin(orgId, currentUserEmail);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        if (!department.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Department does not belong to organization " + orgId);
        }

        String normalizedCode = request.getCode().trim().toUpperCase();

        if (categoryRepository.existsByOrganizationIdAndCode(orgId, normalizedCode)) {
            throw new DuplicateResourceException(
                    "Category with code '" + normalizedCode + "' already exists in organization " + organization.getName());
        }

        IssueCategory category = new IssueCategory(
                UUID.randomUUID(),
                organization,
                department,
                normalizedCode,
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getSubtypes() != null ? request.getSubtypes().trim() : null,
                request.getDefaultPriority(),
                request.isRequiresAsset()
        );

        IssueCategory saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(UUID orgId, UUID departmentId, String currentUserEmail) {
        // Enforce organization data isolation
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        List<IssueCategory> categories;
        if (departmentId != null) {
            categories = categoryRepository.findByOrganizationIdAndDepartmentIdAndActiveTrue(orgId, departmentId);
        } else {
            categories = categoryRepository.findByOrganizationIdAndActiveTrue(orgId);
        }

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID orgId, UUID categoryId, String currentUserEmail) {
        // Enforce organization data isolation
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        IssueCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        if (!category.getOrganization().getId().equals(orgId)) {
            throw new ResourceNotFoundException("Category " + categoryId + " does not belong to organization " + orgId);
        }

        return mapToResponse(category);
    }

    private CategoryResponse mapToResponse(IssueCategory cat) {
        return new CategoryResponse(
                cat.getId(),
                cat.getOrganization().getId(),
                cat.getDepartment().getId(),
                cat.getDepartment().getName(),
                cat.getCode(),
                cat.getName(),
                cat.getDescription(),
                cat.getSubtypes(),
                cat.getDefaultPriority(),
                cat.isRequiresAsset(),
                cat.isActive(),
                cat.getCreatedAt()
        );
    }
}
