-- Free-form key → value store per locale + namespace, versioned.
CREATE TABLE messages (
    locale     VARCHAR(8)   NOT NULL,
    namespace  VARCHAR(40)  NOT NULL DEFAULT 'default',
    key        VARCHAR(120) NOT NULL,
    value      TEXT         NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (locale, namespace, key)
);
CREATE INDEX idx_messages_locale ON messages (locale, namespace);

-- Reference entries that need localization: professions, visa types,
-- regulation flags, anerkennung stages, job categories, etc.
CREATE TABLE reference_entries (
    type       VARCHAR(40)  NOT NULL,
    code       VARCHAR(80)  NOT NULL,
    sort_order INTEGER      NOT NULL DEFAULT 0,
    metadata   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    PRIMARY KEY (type, code)
);

CREATE TABLE reference_entry_translations (
    type    VARCHAR(40)  NOT NULL,
    code    VARCHAR(80)  NOT NULL,
    locale  VARCHAR(8)   NOT NULL,
    label   TEXT         NOT NULL,
    hint    TEXT,
    PRIMARY KEY (type, code, locale),
    FOREIGN KEY (type, code) REFERENCES reference_entries (type, code) ON DELETE CASCADE
);

-- Professions dictionary: used by the onboarding autocomplete.
CREATE TABLE professions (
    code           VARCHAR(80) PRIMARY KEY,
    regulated      BOOLEAN     NOT NULL DEFAULT FALSE,
    category_hint  VARCHAR(20),
    skills         TEXT        NOT NULL DEFAULT '' -- comma separated
);

CREATE TABLE profession_translations (
    profession_code VARCHAR(80) NOT NULL REFERENCES professions (code) ON DELETE CASCADE,
    locale          VARCHAR(8)  NOT NULL,
    label           TEXT        NOT NULL,
    PRIMARY KEY (profession_code, locale)
);

CREATE INDEX idx_profession_translations_locale ON profession_translations (locale);
CREATE INDEX idx_profession_translations_label ON profession_translations USING gin (lower(label) gin_trgm_ops);
