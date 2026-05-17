-- Create a dedicated schema + role per microservice so schemas stay isolated
-- even when they share one Postgres instance locally.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Schema per service
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS jobs;
CREATE SCHEMA IF NOT EXISTS applications;
CREATE SCHEMA IF NOT EXISTS companies;
CREATE SCHEMA IF NOT EXISTS immigration;
CREATE SCHEMA IF NOT EXISTS documents;
CREATE SCHEMA IF NOT EXISTS matching;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS i18n;

-- All schemas owned by the bewerbi user in dev; in prod you'd have one role
-- per service with GRANTs restricted to the matching schema.
ALTER SCHEMA identity     OWNER TO bewerbi;
ALTER SCHEMA jobs         OWNER TO bewerbi;
ALTER SCHEMA applications OWNER TO bewerbi;
ALTER SCHEMA companies    OWNER TO bewerbi;
ALTER SCHEMA immigration  OWNER TO bewerbi;
ALTER SCHEMA documents    OWNER TO bewerbi;
ALTER SCHEMA matching     OWNER TO bewerbi;
ALTER SCHEMA notification OWNER TO bewerbi;
ALTER SCHEMA i18n         OWNER TO bewerbi;
