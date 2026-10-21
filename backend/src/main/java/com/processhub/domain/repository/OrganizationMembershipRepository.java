package com.processhub.domain.repository;

import com.processhub.domain.entity.OrganizationMembership;
import com.processhub.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, UUID> {

    List<OrganizationMembership> findByUserId(UUID userId);

    List<OrganizationMembership> findByUserIdAndActiveTrue(UUID userId);

    List<OrganizationMembership> findByOrganizationId(UUID organizationId);

    List<OrganizationMembership> findByOrganizationIdAndActiveTrue(UUID organizationId);

    List<OrganizationMembership> findByOrganizationIdAndDepartmentId(UUID organizationId, UUID departmentId);

    Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndActiveTrue(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(UUID organizationId, UUID userId, Role role);
}
