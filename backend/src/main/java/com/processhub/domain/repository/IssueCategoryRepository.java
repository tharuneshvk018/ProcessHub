package com.processhub.domain.repository;

import com.processhub.domain.entity.IssueCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueCategoryRepository extends JpaRepository<IssueCategory, UUID> {

    List<IssueCategory> findByOrganizationIdAndActiveTrue(UUID organizationId);

    List<IssueCategory> findByOrganizationIdAndDepartmentIdAndActiveTrue(UUID organizationId, UUID departmentId);

    Optional<IssueCategory> findByOrganizationIdAndCode(UUID organizationId, String code);

    boolean existsByOrganizationIdAndCode(UUID organizationId, String code);
}
