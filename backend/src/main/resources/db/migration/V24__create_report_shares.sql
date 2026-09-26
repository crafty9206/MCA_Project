CREATE TABLE report_shares (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    report_snapshot TEXT NOT NULL,
    journal_included BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    access_count BIGINT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_report_shares_user_created ON report_shares(user_id, created_at DESC);