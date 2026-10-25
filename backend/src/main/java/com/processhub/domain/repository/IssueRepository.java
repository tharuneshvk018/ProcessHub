package com.processhub.domain.repository;

import com.processhub.domain.entity.Issue;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {

    Optional<Issue> findByIdAndOrganizationId(UUID id, UUID orgId);

    Optional<Issue> findByReferenceCodeAndOrganizationId(String referenceCode, UUID orgId);

    Optional<Issue> findByReferenceCode(String referenceCode);

    List<Issue> findByOrganizationIdOrderByCreatedAtDesc(UUID orgId);

    List<Issue> findByOrganizationIdAndReporterIdOrderByCreatedAtDesc(UUID orgId, UUID reporterId);

    List<Issue> findByOrganizationIdAndDepartmentIdOrderByCreatedAtDesc(UUID orgId, UUID departmentId);

    List<Issue> findByOrganizationIdAndStatusOrderByCreatedAtDesc(UUID orgId, IssueStatus status);

    long countByOrganizationIdAndReporterId(UUID orgId, UUID reporterId);

    long countByOrganizationIdAndReporterIdAndStatus(UUID orgId, UUID reporterId, IssueStatus status);

    long countByOrganizationIdAndReporterIdAndStatusNotIn(UUID orgId, UUID reporterId, Collection<IssueStatus> statuses);

    long countByAssignedTechnicianIdAndStatusNotIn(UUID technicianId, Collection<IssueStatus> statuses);

    List<Issue> findByOrganizationIdAndAssignedTechnicianIdOrderByCreatedAtDesc(UUID orgId, UUID technicianId);

    List<Issue> findByOrganizationIdAndAssignedTechnicianIdAndStatusOrderByCreatedAtDesc(UUID orgId, UUID technicianId, IssueStatus status);

    long countByOrganizationIdAndAssignedTechnicianId(UUID orgId, UUID technicianId);

    long countByOrganizationIdAndAssignedTechnicianIdAndStatus(UUID orgId, UUID technicianId, IssueStatus status);

    List<Issue> findByOrganizationIdAndDepartmentIdInOrderByCreatedAtDesc(UUID orgId, Collection<UUID> departmentIds);

    boolean existsByReporterIdAndOrganizationIdAndDepartmentIdAndCategoryIdAndTitleAndCreatedAtAfter(
            UUID reporterId, UUID orgId, UUID departmentId, UUID categoryId, String title, Instant after);

    List<Issue> findByOrganizationIdAndAssetIdAndCreatedAtAfterAndIdNot(UUID orgId, String assetId, Instant after, UUID id);

    List<Issue> findByOrganizationIdAndLocationBuildingAndLocationRoomAndCategoryIdAndCreatedAtAfterAndIdNot(
            UUID orgId, String building, String room, UUID categoryId, Instant after, UUID id);

    List<Issue> findByOrganizationIdAndClosedAtIsNullAndSlaDueAtBeforeAndSlaBreachedFalse(UUID orgId, Instant now);

    long countByOrganizationIdAndSlaBreachedTrue(UUID orgId);

    long countByOrganizationId(UUID orgId);

    long countByOrganizationIdAndStatus(UUID orgId, IssueStatus status);

    long countByOrganizationIdAndPriority(UUID orgId, Priority priority);

    long countByOrganizationIdAndDepartmentId(UUID orgId, UUID departmentId);

    List<Issue> findByOrganizationIdAndClosedAtIsNotNull(UUID orgId);

    @Query(value = "SELECT nextval('issue_ref_seq')", nativeQuery = true)
    Long getNextIssueReferenceSequence();
}
