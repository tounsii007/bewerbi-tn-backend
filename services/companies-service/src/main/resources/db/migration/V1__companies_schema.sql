CREATE TABLE companies (
    id                    UUID PRIMARY KEY,
    owner_user_id         UUID NOT NULL,
    name                  VARCHAR(120) NOT NULL,
    slug                  VARCHAR(140) NOT NULL UNIQUE,
    description           VARCHAR(2000),
    website               VARCHAR(500),
    logo_url              VARCHAR(500),
    industry              VARCHAR(80),
    size                  VARCHAR(80),
    country               VARCHAR(80),
    city                  VARCHAR(80),
    trade_register_number VARCHAR(80),
    verification_status   VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    verification_note     VARCHAR(500),
    rating_avg            DOUBLE PRECISION,
    rating_count          INTEGER NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL
);

CREATE TABLE company_reviews (
    id                UUID PRIMARY KEY,
    company_id        UUID NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    author_user_id    UUID NOT NULL,
    rating            INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title             VARCHAR(120),
    body              VARCHAR(4000),
    pros              VARCHAR(1000),
    cons              VARCHAR(1000),
    employment_status VARCHAR(40),
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    UNIQUE (company_id, author_user_id)
);
CREATE INDEX idx_reviews_company ON company_reviews (company_id);
