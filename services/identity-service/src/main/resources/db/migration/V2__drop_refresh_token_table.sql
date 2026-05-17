-- Refresh tokens live in Redis now (see RefreshTokenStore), so the JPA
-- collection table is no longer needed. Dropping it also removes the
-- performance hit of fetching it eagerly on every login.
DROP TABLE IF EXISTS user_refresh_tokens;
