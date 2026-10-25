package com.processhub.domain.enums;

public enum IssueStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    MORE_INFO_REQUESTED("More Information Requested"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    AWAITING_FINANCE("Awaiting Finance Approval"),
    RESOLVED_PENDING_VERIFICATION("Resolved - Pending Verification"),
    CLOSED("Closed"),
    REJECTED("Rejected"),
    REOPENED("Reopened");

    private final String displayName;

    IssueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
