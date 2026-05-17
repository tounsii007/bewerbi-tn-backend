CREATE TABLE jobs (
    id                UUID PRIMARY KEY,
    company_id        UUID NOT NULL,
    employer_user_id  UUID NOT NULL,
    title             VARCHAR(200) NOT NULL,
    description       TEXT NOT NULL,
    requirements      TEXT,
    category          VARCHAR(20) NOT NULL,
    type              VARCHAR(20) NOT NULL,
    location          VARCHAR(120) NOT NULL,
    salary_min        INTEGER,
    salary_max        INTEGER,
    salary_currency   VARCHAR(4) DEFAULT 'EUR',
    german_level      VARCHAR(4),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    premium           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_jobs_company ON jobs (company_id);
CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_category_type ON jobs (category, type);
CREATE INDEX idx_jobs_title_trgm ON jobs USING gin (lower(title) gin_trgm_ops);
CREATE INDEX idx_jobs_description_trgm ON jobs USING gin (lower(description) gin_trgm_ops);

CREATE TABLE saved_searches (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    name              VARCHAR(120) NOT NULL,
    query             VARCHAR(200),
    category          VARCHAR(20),
    type              VARCHAR(20),
    location          VARCHAR(120),
    min_german_level  VARCHAR(4),
    salary_min        INTEGER,
    alerts_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    alert_frequency   VARCHAR(16) NOT NULL DEFAULT 'DAILY',
    last_notified_at  TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_saved_searches_user ON saved_searches (user_id);

-- Salary market data by category (fed by an admin job or external source)
CREATE TABLE salary_market (
    category  VARCHAR(20) PRIMARY KEY,
    p25_eur   INTEGER NOT NULL,
    p50_eur   INTEGER NOT NULL,
    p75_eur   INTEGER NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO salary_market (category, p25_eur, p50_eur, p75_eur) VALUES
    ('IT',        48000, 62000, 82000),
    ('PFLEGE',    32000, 38000, 46000),
    ('TRANSPORT', 28000, 34000, 42000),
    ('HANDWERK',  30000, 40000, 52000),
    ('GASTRO',    24000, 30000, 40000),
    ('BAU',       32000, 42000, 58000),
    ('SONSTIGE',  30000, 42000, 58000);
