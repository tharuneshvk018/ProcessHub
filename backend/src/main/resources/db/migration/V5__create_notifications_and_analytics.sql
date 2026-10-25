-- V5: Notifications and SLA Analytics Schema

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    issue_id UUID REFERENCES issues(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_read ON notifications(recipient_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_org_created ON notifications(org_id, created_at);

ALTER TABLE issues ADD COLUMN IF NOT EXISTS sla_due_at TIMESTAMP;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS sla_breached BOOLEAN NOT NULL DEFAULT FALSE;
