package com.processhub.domain.enums;

public enum ExpenditureStatus {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String displayName;

    ExpenditureStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
