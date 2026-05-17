-- Testcontainers runs this before Flyway migrates the service schema.
CREATE SCHEMA IF NOT EXISTS identity;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
