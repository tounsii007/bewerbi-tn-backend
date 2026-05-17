CREATE TABLE anerkennung_cases (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    profession          VARCHAR(120) NOT NULL,
    regulation_type     VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    competent_authority VARCHAR(200),
    stage               VARCHAR(32) NOT NULL DEFAULT 'INFORMATION',
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_anerkennung_user ON anerkennung_cases (user_id);

CREATE TABLE anerkennung_steps (
    id           UUID PRIMARY KEY,
    case_id      UUID NOT NULL REFERENCES anerkennung_cases (id) ON DELETE CASCADE,
    title_key    VARCHAR(120) NOT NULL,
    desc_key     VARCHAR(120),
    sort_order   INTEGER NOT NULL,
    completed_at TIMESTAMPTZ,
    document_id  UUID,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);

CREATE TABLE visa_cases (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL,
    visa_type        VARCHAR(32) NOT NULL,
    stage            VARCHAR(32) NOT NULL DEFAULT 'PREPARATION',
    appointment_date DATE,
    embassy_city     VARCHAR(80),
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_visa_user ON visa_cases (user_id);

CREATE TABLE visa_requirements (
    id           UUID PRIMARY KEY,
    case_id      UUID NOT NULL REFERENCES visa_cases (id) ON DELETE CASCADE,
    title_key    VARCHAR(120) NOT NULL,
    desc_key     VARCHAR(120),
    required     BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order   INTEGER NOT NULL,
    completed_at TIMESTAMPTZ,
    document_id  UUID,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);
