CREATE TABLE users (
    id                          UUID PRIMARY KEY,
    email                       VARCHAR(255) NOT NULL UNIQUE,
    password_hash               VARCHAR(255) NOT NULL,
    role                        VARCHAR(20)  NOT NULL,
    email_verified              BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verification_token    VARCHAR(64),
    email_verification_expires_at TIMESTAMPTZ,
    preferred_locale            VARCHAR(8)   NOT NULL DEFAULT 'de',
    last_login_at               TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL,
    updated_at                  TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_users_email_verification ON users (email_verification_token);

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
