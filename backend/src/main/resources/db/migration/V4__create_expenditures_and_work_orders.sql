-- V4: Expenditure Requests and Resolution Verification Schema

CREATE TABLE IF NOT EXISTS expenditure_requests (
    id UUID PRIMARY KEY,
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    requested_by_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    amount NUMERIC(12, 2) NOT NULL,
    justification VARCHAR(1000) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    decided_by_id UUID REFERENCES users(id) ON DELETE SET NULL,
    decision_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    decided_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_expenditures_issue_id ON expenditure_requests(issue_id);
CREATE INDEX IF NOT EXISTS idx_expenditures_status ON expenditure_requests(status);

ALTER TABLE issues ADD COLUMN IF NOT EXISTS work_summary VARCHAR(2000);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS root_cause VARCHAR(1000);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS labor_hours NUMERIC(6, 2);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS parts_used VARCHAR(1000);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS resolution_evidence_url VARCHAR(500);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS verified_by_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS verification_notes VARCHAR(1000);
