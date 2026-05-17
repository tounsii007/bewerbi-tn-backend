-- ============================================================
-- bewerbi.tn — Core schema (Flyway V1)
-- PostgreSQL 16
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- -----------------------------------------------------------
-- Users & profiles
-- -----------------------------------------------------------
CREATE TABLE users (
    id                          UUID PRIMARY KEY,
    email                       VARCHAR(255) NOT NULL UNIQUE,
    password_hash               VARCHAR(255) NOT NULL,
    role                        VARCHAR(20)  NOT NULL,
    email_verified              BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verification_token    VARCHAR(64),
    email_verification_expires_at TIMESTAMPTZ,
    last_login_at               TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL,
    updated_at                  TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_refresh_tokens (
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, token_hash)
);

CREATE TABLE profiles (
    id                   UUID PRIMARY KEY,
    user_id              UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    first_name           VARCHAR(80),
    last_name            VARCHAR(80),
    phone                VARCHAR(32),
    city                 VARCHAR(80),
    country              VARCHAR(80),
    bio                  VARCHAR(2000),
    photo_url            VARCHAR(500),
    desired_profession   VARCHAR(120),
    german_level         VARCHAR(4),
    recognition_status   VARCHAR(32),
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL
);

CREATE TABLE profile_skills (
    profile_id UUID NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    skill      VARCHAR(80) NOT NULL
);
CREATE INDEX idx_profile_skills_profile ON profile_skills (profile_id);

-- -----------------------------------------------------------
-- Companies & reviews
-- -----------------------------------------------------------
CREATE TABLE companies (
    id                     UUID PRIMARY KEY,
    owner_user_id          UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name                   VARCHAR(120) NOT NULL,
    slug                   VARCHAR(140) NOT NULL UNIQUE,
    description            VARCHAR(2000),
    website                VARCHAR(500),
    logo_url               VARCHAR(500),
    industry               VARCHAR(80),
    size                   VARCHAR(80),
    country                VARCHAR(80),
    city                   VARCHAR(80),
    trade_register_number  VARCHAR(80),
    verification_status    VARCHAR(20)  NOT NULL DEFAULT 'UNVERIFIED',
    verification_note      VARCHAR(500),
    rating_avg             DOUBLE PRECISION,
    rating_count           INTEGER NOT NULL DEFAULT 0,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_companies_owner ON companies (owner_user_id);
CREATE INDEX idx_companies_verification ON companies (verification_status);

CREATE TABLE company_reviews (
    id                 UUID PRIMARY KEY,
    company_id         UUID NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    author_user_id     UUID NOT NULL REFERENCES users (id),
    rating             INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title              VARCHAR(120),
    body               VARCHAR(4000),
    pros               VARCHAR(1000),
    cons               VARCHAR(1000),
    employment_status  VARCHAR(40),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    UNIQUE (company_id, author_user_id)
);
CREATE INDEX idx_reviews_company ON company_reviews (company_id);
CREATE INDEX idx_reviews_author ON company_reviews (author_user_id);

-- -----------------------------------------------------------
-- Jobs
-- -----------------------------------------------------------
CREATE TABLE jobs (
    id                UUID PRIMARY KEY,
    company_id        UUID NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    employer_user_id  UUID NOT NULL REFERENCES users (id),
    title             VARCHAR(200) NOT NULL,
    description       TEXT NOT NULL,
    requirements      TEXT,
    category          VARCHAR(20) NOT NULL,
    type              VARCHAR(20) NOT NULL,
    location          VARCHAR(120) NOT NULL,
    salary_min        INTEGER,
    salary_max        INTEGER,
    salary_currency   VARCHAR(4),
    german_level      VARCHAR(4),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    premium           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_jobs_company ON jobs (company_id);
CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_category_type ON jobs (category, type);
CREATE INDEX idx_jobs_title_trgm ON jobs USING gin (title gin_trgm_ops);
CREATE INDEX idx_jobs_description_trgm ON jobs USING gin (description gin_trgm_ops);

-- -----------------------------------------------------------
-- Applications & favorites
-- -----------------------------------------------------------
CREATE TABLE applications (
    id                 UUID PRIMARY KEY,
    job_id             UUID NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    applicant_user_id  UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
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
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    job_id     UUID NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, job_id)
);
CREATE INDEX idx_favorites_user ON favorites (user_id);

-- -----------------------------------------------------------
-- Saved searches (with alerts)
-- -----------------------------------------------------------
CREATE TABLE saved_searches (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name              VARCHAR(120) NOT NULL,
    query             VARCHAR(200),
    category          VARCHAR(20),
    type              VARCHAR(20),
    location          VARCHAR(120),
    min_german_level  VARCHAR(4),
    salary_min        INTEGER,
    alerts_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_saved_searches_user ON saved_searches (user_id);

-- -----------------------------------------------------------
-- Anerkennung
-- -----------------------------------------------------------
CREATE TABLE anerkennung_cases (
    id                    UUID PRIMARY KEY,
    user_id               UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    profession            VARCHAR(120) NOT NULL,
    regulation_type       VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    competent_authority   VARCHAR(200),
    stage                 VARCHAR(32) NOT NULL DEFAULT 'INFORMATION',
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_anerkennung_user ON anerkennung_cases (user_id);

CREATE TABLE anerkennung_steps (
    id            UUID PRIMARY KEY,
    case_id       UUID NOT NULL REFERENCES anerkennung_cases (id) ON DELETE CASCADE,
    title         VARCHAR(140) NOT NULL,
    description   VARCHAR(1000),
    sort_order    INTEGER NOT NULL,
    completed_at  TIMESTAMPTZ,
    document_id   UUID,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_anerkennung_steps_case ON anerkennung_steps (case_id);

-- -----------------------------------------------------------
-- Visa tracker
-- -----------------------------------------------------------
CREATE TABLE visa_cases (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    visa_type         VARCHAR(32) NOT NULL,
    stage             VARCHAR(32) NOT NULL DEFAULT 'PREPARATION',
    appointment_date  DATE,
    embassy_city      VARCHAR(80),
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_visa_user ON visa_cases (user_id);

CREATE TABLE visa_requirements (
    id            UUID PRIMARY KEY,
    case_id       UUID NOT NULL REFERENCES visa_cases (id) ON DELETE CASCADE,
    title         VARCHAR(140) NOT NULL,
    description   VARCHAR(1000),
    required      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order    INTEGER NOT NULL,
    completed_at  TIMESTAMPTZ,
    document_id   UUID,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_visa_req_case ON visa_requirements (case_id);

-- -----------------------------------------------------------
-- Documents
-- -----------------------------------------------------------
CREATE TABLE documents (
    id              UUID PRIMARY KEY,
    owner_user_id   UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type            VARCHAR(32) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    storage_path    VARCHAR(500) NOT NULL,
    content_type    VARCHAR(80),
    size_bytes      BIGINT,
    parsed_text     TEXT,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_documents_owner ON documents (owner_user_id);
