package com.processhub.domain.enums;

public enum RecurrenceLevel {
    NONE("No Prior Occurrences"),
    LOW("Isolated Past Occurrence"),
    ELEVATED("Elevated Recurrence"),
    CHRONIC_REPEAT("Chronic Repeat Failure");

    private final String displayName;

    RecurrenceLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
