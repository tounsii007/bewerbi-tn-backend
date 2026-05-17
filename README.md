# bewerbi.tn — Backend (Microservices)

Modernes Microservices-Backend für bewerbi.tn. Spring Boot 3.4 auf Java 21 mit Virtual Threads, Spring Cloud Gateway 2024.0, PostgreSQL 16 (schema-per-service), Redis 7, Flyway, OpenTelemetry-ready, serverseitige Lokalisierung.

## Architektur

```
                     ┌───────────────┐
 Client (Web/iOS/ ─► │    Gateway    │ :8080
    Flutter/Web)     │ Spring Cloud  │  JWT verification · CORS · routing
                     │   Gateway     │
                     └───────┬───────┘
                             │
     ┌───────────────────────┼───────────────────────┐
     ▼                       ▼                       ▼
┌─────────┐           ┌─────────────┐         ┌─────────────┐
│identity │           │    jobs     │         │ immigration │
│ :8081   │           │   :8082     │         │   :8085     │
│ auth    │           │ jobs        │         │ anerkennung │
│ profile │           │ saved srch  │         │ visa        │
│ onboard │           │ salary mkt  │         │             │
└────┬────┘           └──────┬──────┘         └──────┬──────┘
     │                       │                        │
     │    ┌──────────────┐   │    ┌──────────────┐    │
     └───►│ i18n-service │◄──┴───►│notification  │◄───┘
          │   :8181      │        │   :8088      │
          │ messages DB  │        │ email (SMTP) │
          │ professions  │        │ push ready   │
          │ ref-data     │        └──────────────┘
          └──────────────┘
                 ▲
     ┌───────────┼───────────┐
     ▼           ▼           ▼
┌─────────┐ ┌─────────┐ ┌──────────┐
│company  │ │documents│ │ matching │
│ :8084   │ │  :8086  │ │   :8087  │
│ reviews │ │ CV parse│ │ match eng│
│ verify  │ │ uploads │ │          │
└─────────┘ └─────────┘ └──────────┘
```

### Services

| Service | Port | Zuständigkeit |
|---|---|---|
| **gateway** | 8080 | JWT-Verifikation, CORS, Routing, Resilience4j |
| **identity-service** | 8081 | Auth (Register/Login/Refresh), User, Profile, Vollständigkeit, Onboarding, Locale-Persistenz |
| **i18n-service** | 8181 | Messages (Keys × Locales), Professions-Dictionary, Reference-Data (Visa-Typen, Anerkennungs-Stufen, Kategorien) |
| **jobs-service** | 8082 | Jobs + Filter/Suche (JPA Specs + pg_trgm), Saved Searches mit Alert-Frequenz, Salary-Market-Daten |
| **applications-service** | 8083 | Bewerbungen, Favoriten |
| **companies-service** | 8084 | Firmenprofile, Reviews, Verifikations-Workflow |
| **immigration-service** | 8085 | Anerkennung-Cases + Visa-Cases mit typ-spezifischen Checklisten; Titel/Beschreibungen via i18n-Keys |
| **documents-service** | 8086 | Upload + PDF-Textextraktion (PDFBox 3) + CV-Autofill-Heuristik |
| **matching-service** | 8087 | Recommendations — ruft jobs + identity via JWT-Forward, Circuit-Breaker |
| **notification-service** | 8088 | E-Mail-Versand, Templates via i18n-service, Push-ready |

### Shared-Libs

| Library | Inhalt |
|---|---|
| `shared/common-api` | `ApiError`, `GlobalExceptionHandler`, `CurrentUser`, Exceptions mit `messageKey` |
| `shared/common-security` | `JwtSecurityConfig` (Decoder/Encoder/CORS), default `SecurityFilterChainRegistrar` |
| `shared/common-i18n` | `LocaleContext` (ThreadLocal-Filter aus `Accept-Language`/`X-Locale`) + `MessageClient` (Redis-cached Client zum i18n-service) |
| `shared/common-events` | `Topics`, `DomainEvents` (typisierte Records), `EventPublisher` (Kafka + JSON) |

## Serverseitige Lokalisierung

Drei Schichten:

1. **`LocaleContext` Filter** (in jedem Service via `common-i18n`) liest `Accept-Language` bzw. `X-Locale`, bindet es an einen ThreadLocal und setzt `Content-Language` in der Antwort.
2. **`MessageClient`** ist ein Resilience4j-geschützter REST-Client zum `i18n-service`. Bundles werden pro Locale für 5 min in Redis gecacht.
3. **`i18n-service`** hält alle lokalisierten Strings in Postgres — Messages (`V2__seed_messages.sql`) und Reference-Entries (`V3__seed_reference_and_professions.sql`). Änderungen ohne Redeploy.

Beispiel: `identity-service` gibt bei `GET /api/v1/profile/me` **lokalisierte** Labels für jedes fehlende Pflichtfeld zurück — auf Basis des Accept-Language-Headers. Das Frontend zeigt sie 1:1 an, ohne eigene Übersetzungstabelle.

Ebenso rendert `immigration-service` die Titel der Anerkennungs- und Visa-Schritte über `messages.resolveIn(locale, titleKey)`. Die Datenbank speichert nur Keys wie `visa.bluecard.salary.title`; der Text kommt aus `i18n-service`.

Alle Fehlerantworten (`ApiError`) haben ein `messageKey`-Feld, sodass Clients entweder den Server-Text oder ihre eigene Übersetzung anzeigen können.

## Datenmodell

Gemeinsame Postgres-Instanz, getrennte **Schemas** pro Service. Keine Foreign-Keys über Service-Grenzen — Service-zu-Service-Konsistenz über ID-Referenzen + Events (letzteres über Redis Pub/Sub vorbereitet, Hook für Kafka).

```
identity.*      users, user_refresh_tokens, profiles, profile_skills
i18n.*          messages, reference_entries, reference_entry_translations,
                professions, profession_translations
jobs.*          jobs, saved_searches, salary_market
applications.*  applications, favorites
companies.*     companies, company_reviews
immigration.*   anerkennung_cases, anerkennung_steps,
                visa_cases, visa_requirements
documents.*     documents
```

## Quickstart

Build-System: **Maven** (multi-module reactor). Java 21 Toolchain, Spring Boot 3.4 Parent-POM.

```bash
# 1. Einmalig: alle Module bauen (lädt Dependencies)
./mvnw install -DskipTests

# 2. Infrastruktur starten (Postgres, Redis, MailHog, Kafka, Jaeger)
docker compose up -d

# 3. Einen Service lokal hoch (Spring Boot Plugin)
./mvnw -pl services/i18n-service -am spring-boot:run
./mvnw -pl services/identity-service -am spring-boot:run
./mvnw -pl services/jobs-service -am spring-boot:run
# …
./mvnw -pl gateway -am spring-boot:run

# oder gleich die komplette Flotte in Containern:
docker compose -f compose.yaml -f compose.services.yaml up --build
```

`-pl <modul> -am` (also-make) baut auch alle abhängigen Module (Shared-Libs).

### Swagger UIs

- Gateway OpenAPI-Sammlung: http://localhost:8080/docs
- identity-service: http://localhost:8081/swagger-ui
- i18n-service: http://localhost:8181/swagger-ui
- jobs-service: http://localhost:8082/swagger-ui
- … (`:808X/swagger-ui` pro Service)

### Auth & Token-Lifecycle

1. `POST /api/v1/auth/register` / `/login` → `{ accessToken, accessTokenExpiresAt, accessTokenExpiresIn, refreshToken, refreshTokenExpiresAt, user }`.
2. Access-Tokens sind HMAC-JWTs (60 min TTL) mit Claims `sub, email, roles, locale, token_type=access`.
3. Refresh-Tokens leben in **Redis** (`auth:refresh:<userId>:<sha256>` mit TTL = Refresh-TTL). Klar-Tokens werden nie gespeichert.
4. `POST /api/v1/auth/refresh` **rotiert**: alter Hash wird gelöscht, ein frischer Pair ausgegeben. Reuse desselben Refresh-Tokens → Ablehnung.
5. `POST /api/v1/auth/logout` widerruft nur den übergebenen Refresh-Token; `/logout-all` löscht alle Refresh-Hashes des Users (Passwort-Reset-Sicherheit).
6. Clients refreshen **proaktiv** 60 s vor `accessTokenExpiresAt`; Web- und Flutter-Client coalescen parallele Refresh-Aufrufe, damit nicht doppelt rotiert wird.
7. Verify-E-Mail: `identity-service` generiert Token beim Register, reicht ihn im `UserRegistered`-Event weiter; `notification-service` baut `https://app/verify?token=<token>&lang=<locale>` und sendet in der Locale des Users.

### Rate-Limiting

Gateway hat einen globalen Redis-Rate-Limiter (`60 req/s burst 120 pro User`) plus einen engeren Bucket nur für `/auth/login|register|refresh` (`5 req/s burst 10`) gegen Credential-Stuffing. Key-Resolver priorisiert JWT-Subject, fällt auf Client-IP zurück.

### Service-Discovery

Für lokale Entwicklung statische URLs per `routes.*` im Gateway. Im Produktiv-Deployment ersetzt man diese mit Kubernetes-DNS (`http://identity-service.default.svc.cluster.local`) oder einer Service-Registry (Eureka, Consul).

### Event-Bus (Kafka)

Asynchrone Inter-Service-Kommunikation über Kafka (KRaft-Mode, kein Zookeeper).

| Topic | Publisher | Consumer |
|---|---|---|
| `bewerbi.users.registered` | identity-service | notification-service → Verify-Mail |
| `bewerbi.applications.created` | applications-service | notification-service, matching-service |
| `bewerbi.companies.verified` | companies-service | notification-service → Arbeitgeber-Hinweis |
| `bewerbi.jobs.published` | jobs-service | matching-service, notification-service (Saved-Search-Alerts) |

Events sind typisierte `record`s in `shared/common-events` → `DomainEvents`; Serialisierung über Jackson. Publisher via `EventPublisher.publish(topic, key, event)`, Consumer via `@KafkaListener` (JSON-String + `ObjectMapper`).

Kafka lokal: `docker compose up kafka` (Port 9092). Bootstrap-Server in jedem Service via `KAFKA_BOOTSTRAP=localhost:9092`.

### Admin-API für i18n-Service

Live-Editing von Messages ohne Redeploy — nur für Admins:

```bash
# Alle Keys in einer Locale listen
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  "http://localhost:8080/api/v1/admin/i18n/messages?locale=fr"

# Einzelnen Key aktualisieren (Cache wird sofort invalidiert)
curl -X PUT -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"locale":"de","key":"tier.starter","value":"Anfänger"}' \
  http://localhost:8080/api/v1/admin/i18n/messages

# Batch-Upload
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"locale":"ar","key":"profile.bio","value":"نبذة"}]' \
  http://localhost:8080/api/v1/admin/i18n/messages/batch
```

Änderungen propagieren zu allen Services in max. 5 Minuten (TTL ihrer Redis-Caches).

### Observability

- **Metrics**: Actuator + Prometheus-Endpoint (`/actuator/prometheus`) in jedem Service.
- **Tracing**: Micrometer-Bridge eingebunden; Jaeger/OTLP-Endpoint unter `http://localhost:4317` (compose.yaml).
- **Logs**: Strukturierte Logs, Level pro Service konfigurierbar in `application.yml`.

## Tech-Stack (alles Stand 2026)

- Java 21 LTS (Virtual Threads, Record-Patterns)
- Spring Boot 3.4.1
- Spring Cloud Gateway 2024.0
- Spring Security 6 (OAuth2 Resource Server, HMAC-SHA256 JWTs via Nimbus JOSE)
- Spring Data JPA + Hibernate 6.6
- PostgreSQL 16 + pg_trgm GIN-Indizes für Volltext
- Flyway 11 pro Service
- Redis 7.4 (MessageClient Cache, Session-Cache)
- Resilience4j 2.2 (Circuit Breaker, Retry, TimeLimiter)
- Apache PDFBox 3 (CV-Parser)
- MapStruct 1.6 (DTO-Mapping)
- springdoc-openapi 2.7
- Maven 3.9 (multi-module reactor, Spring Boot Parent-POM)
- Apache Kafka 7.7 (KRaft-Mode, kein Zookeeper)
- Testcontainers 1.20 für Integrationstests

## Konfiguration

Alle Secrets und externen URLs via Environment-Variablen:

```
JWT_SECRET           Gemeinsames HMAC-Secret (≥ 256 bit in Produktion)
DB_URL, DB_USER      Postgres-Credentials
REDIS_HOST, REDIS_PORT
MAIL_HOST, MAIL_PORT
UPLOAD_ROOT          nur documents-service
I18N_SERVICE_URL     jeder Service, der den MessageClient nutzt
ANTHROPIC_API_KEY    optional (Cover-Letter-Generierung — künftig)
STRIPE_SECRET        optional (Employer-Premium — künftig)
```

## Nächste Schritte

- **Eureka/Consul** statt statischer Gateway-Routes
- **Service-Mesh** (Istio/Linkerd) für mTLS
- **CI**: Maven-Repo-Cache + Testcontainers in GitHub Actions
- **CD**: Helm-Charts pro Service mit Ingress
- **Migration** von Refresh-Tokens nach Redis (aktuell JPA im identity-service)
