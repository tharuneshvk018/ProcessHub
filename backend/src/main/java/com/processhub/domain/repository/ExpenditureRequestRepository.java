package com.processhub.domain.repository;

import com.processhub.domain.entity.ExpenditureRequest;
import com.processhub.domain.enums.ExpenditureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenditureRequestRepository extends JpaRepository<ExpenditureRequest, UUID> {

    List<ExpenditureRequest> findByIssueIdOrderByCreatedAtAsc(UUID issueId);

    List<ExpenditureRequest> findByIssueOrganizationIdOrderByCreatedAtDesc(UUID orgId);

    List<ExpenditureRequest> findByIssueOrganizationIdAndStatusOrderByCreatedAtDesc(UUID orgId, ExpenditureStatus status);

    long countByIssueOrganizationIdAndStatus(UUID orgId, ExpenditureStatus status);

    long countByIssueOrganizationId(UUID orgId);
}
