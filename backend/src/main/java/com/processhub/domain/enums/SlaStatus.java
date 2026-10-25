package com.processhub.domain.enums;

public enum SlaStatus {
    ON_TRACK("On Track"),
    AT_RISK("At Risk (<25% Time Remaining)"),
    BREACHED("SLA Breached");

    private final String displayName;

    SlaStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
