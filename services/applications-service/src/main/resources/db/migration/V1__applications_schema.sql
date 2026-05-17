CREATE TABLE applications (
    id                 UUID PRIMARY KEY,
    job_id             UUID NOT NULL,
    applicant_user_id  UUID NOT NULL,
    cover_letter       TEXT,
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    match_score        INTEGER,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    UNIQUE (job_id, applicant_user_id)
);
CREATE INDEX idx_apps_applicant ON applications (applicant_user_id);
CREATE INDEX idx_apps_job ON applications (job_id);

CREATE TABLE favorites (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    job_id     UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, job_id)
);
CREATE INDEX idx_favorites_user ON favorites (user_id);
