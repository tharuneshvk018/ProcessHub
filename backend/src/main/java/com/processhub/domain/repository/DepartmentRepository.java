package com.processhub.domain.repository;

import com.processhub.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByOrganizationId(UUID organizationId);

    List<Department> findByOrganizationIdAndActiveTrue(UUID organizationId);

    Optional<Department> findByOrganizationIdAndCode(UUID organizationId, String code);

    boolean existsByOrganizationIdAndCode(UUID organizationId, String code);
}
