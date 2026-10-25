package com.processhub.domain.repository;

import com.processhub.domain.entity.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, UUID> {

    List<IssueAttachment> findByIssueIdOrderByCreatedAtAsc(UUID issueId);

    Optional<IssueAttachment> findByIdAndIssueId(UUID id, UUID issueId);
}
