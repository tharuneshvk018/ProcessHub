package com.processhub.domain.repository;

import com.processhub.domain.entity.IssueTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueTimelineEventRepository extends JpaRepository<IssueTimelineEvent, UUID> {

    List<IssueTimelineEvent> findByIssueIdOrderByCreatedAtAsc(UUID issueId);
}
