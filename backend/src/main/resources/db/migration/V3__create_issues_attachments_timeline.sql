-- V3: Issues, Attachments, and Timeline Events Schema

CREATE SEQUENCE IF NOT EXISTS issue_ref_seq START WITH 1001 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS issues (
    id UUID PRIMARY KEY,
    reference_code VARCHAR(30) NOT NULL UNIQUE,
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    category_id UUID NOT NULL REFERENCES issue_categories(id) ON DELETE RESTRICT,
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    subtype VARCHAR(100),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location_building VARCHAR(100) NOT NULL,
    location_room VARCHAR(100) NOT NULL,
    asset_id VARCHAR(100),
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    assigned_technician_id UUID REFERENCES users(id) ON DELETE SET NULL,
    current_stage VARCHAR(100) NOT NULL DEFAULT 'SUBMITTED',
    blocking_reason VARCHAR(500),
    responsible_role VARCHAR(100) NOT NULL DEFAULT 'ROLE_SUPERVISOR',
    next_action VARCHAR(500) NOT NULL DEFAULT 'Awaiting supervisor triage and assignment',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS issue_attachments (
    id UUID PRIMARY KEY,
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500),
    file_data BYTEA,
    uploaded_by_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS issue_timeline_events (
    id UUID PRIMARY KEY,
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_issues_org_id ON issues(org_id);
CREATE INDEX IF NOT EXISTS idx_issues_reporter_id ON issues(reporter_id);
CREATE INDEX IF NOT EXISTS idx_issues_dept_id ON issues(department_id);
CREATE INDEX IF NOT EXISTS idx_issues_cat_id ON issues(category_id);
CREATE INDEX IF NOT EXISTS idx_issues_status ON issues(status);
CREATE INDEX IF NOT EXISTS idx_issues_ref_code ON issues(reference_code);
CREATE INDEX IF NOT EXISTS idx_issues_asset_id ON issues(asset_id);
CREATE INDEX IF NOT EXISTS idx_attachments_issue_id ON issue_attachments(issue_id);
CREATE INDEX IF NOT EXISTS idx_timeline_issue_id ON issue_timeline_events(issue_id);
