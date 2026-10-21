package com.processhub.domain.enums;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    ROLE_REPORTER("Reporter - submits and tracks issues"),
    ROLE_SUPERVISOR("Supervisor - triages, reviews, and assigns department issues"),
    ROLE_TECHNICIAN("Technician - resolves assigned work orders"),
    ROLE_FINANCE_APPROVER("Finance Approver - reviews and authorizes expenditures"),
    ROLE_ADMIN("Organization Administrator - full system governance");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(this.name());
    }
}
