CREATE TABLE documents (
    id            UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    type          VARCHAR(32) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    storage_path  VARCHAR(500) NOT NULL,
    content_type  VARCHAR(80),
    size_bytes    BIGINT,
    parsed_text   TEXT,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_documents_owner ON documents (owner_user_id);
