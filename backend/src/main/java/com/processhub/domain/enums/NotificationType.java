package com.processhub.domain.enums;

public enum NotificationType {
    ISSUE_SUBMITTED("New Issue Reported"),
    ISSUE_ASSIGNED("Work Order Assigned"),
    STATUS_CHANGED("Status Updated"),
    RFI_REQUESTED("Information Requested"),
    RFI_PROVIDED("Information Provided"),
    EXPENDITURE_REQUESTED("Expenditure Authorization Requested"),
    EXPENDITURE_DECIDED("Expenditure Authorization Decided"),
    RESOLUTION_SUBMITTED("Resolution Submitted for Verification"),
    VERIFICATION_COMPLETED("Resolution Verification Completed"),
    SLA_BREACH_WARNING("SLA Escalation Warning");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
