## ARAW Backend

Domain-driven Spring Boot backend for the African Women Researchers platform. It powers storytelling, public profiles for participants and volunteers, rich media management via MinIO, and publication workflows with email notifications.

### Bounded Contexts
- **Content** – Articles and stories (`/api/articles`) with publication lifecycle, tagging, hero media references, and slug management.
- **Community** – Participant, volunteer, mentor, and donor profiles (`/api/community/profiles`) with feature and activation toggles.
- **Media** – Object storage abstraction (`/api/media`) backed by MinIO for images, documents, and other uploads.
- **Notifications** – Publication email fan-out using Gmail SMTP once articles move to the published state.

### Domain-Driven Design Notes
- **Aggregates**: `AlumniClass` (students, gallery items), `Participant`, `Event`, and `Application` enforce invariants and own ordering/display rules; entities expose intentful methods rather than field mutation.
- **Application services**: Orchestrate use cases and transactions (creation, updates, ordering, feedback submission) while delegating validation to aggregates and collaborating with domain services (`ParticipantDomainService`, `FeedbackDomainService`).
- **Repositories**: Domain-facing interfaces live under `domain.*.repository` with JPA adapters in `infrastructure.*.persistence` to keep persistence concerns isolated.
- **Cross-cutting**: Email and media storage sit behind services (`TemplatedEmailService`, `MediaStorageService`) injected into application services; tests use in-memory infra and stubbed config.

### Prerequisites
- Java 21+ (project targets Java 25 to align with `pom.xml`)
- Maven 3.9+
- PostgreSQL (default dev DSN `jdbc:postgresql://localhost:5432/springboot_db`)
- MinIO server (defaults to `http://localhost:9000`, bucket `araw-media`)
- Gmail SMTP credentials with an app password for `spring.mail.*`

### Configuration
Override with environment variables or profile-specific `application.yml`.

| Purpose | Property | Default |
| --- | --- | --- |
| Database | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | `jdbc:postgresql://localhost:5432/springboot_db`, `postgres`, `postgres` |
| JPA | `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |
| MinIO | `MINIO_ENDPOINT`, `MINIO_BUCKET`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_REGION`, `MINIO_SECURE` | `http://localhost:9000`, `araw-media`, `minioadmin`, `minioadmin`, `us-east-1`, `false` |
| Media URLs | `MINIO_PRESIGNED_EXPIRY_MINUTES` | `60` |
| Notifications | `PUBLICATION_NOTIFICATION_RECIPIENTS`, `PUBLICATION_SITE_URL`, `PUBLICATION_NOTIFICATION_FROM` | _(none)_, `https://www.ara-w.org`, _(derived from `spring.mail.username`)_ |
| Notifications Email | `APPLICATION_BASE_URL`, `FEEDBACK_BASE_URL` | `https://apply.ara-w.org/events`, _(none)_ |
| Gmail SMTP | `MAIL_USERNAME`, `MAIL_PASSWORD` | _(none)_ |

Test profile (`src/test/resources/application.yml`) runs in-memory H2, disables publication emails, and points mail to a dummy SMTP host.

### Running Locally
```bash
mvn spring-boot:run
```
Use `docker compose` to spin up PostgreSQL and MinIO if desired. Ensure MinIO credentials match the defaults or override via environment variables.

```bash
# launch both backing services (data persists in named volumes)
docker compose up -d postgres minio
```

If using custom `POSTGRES_*` credentials from `.env.deploy`, either run `./deploy-backend.sh` directly or source `.env.deploy` before manually running `docker compose`; Postgres only applies `POSTGRES_USER` / `POSTGRES_PASSWORD` the first time the data volume is initialized.

The MinIO console is available at `http://localhost:${HOST_MINIO_CONSOLE_PORT:-9901}` with the credentials `minioadmin` / `minioadmin` (or whatever you set in `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD`). Create the `araw-media` bucket once and the API will reuse it for uploads.

For servers where Postgres/MinIO default ports are already in use, this project supports configurable host ports:

- `HOST_POSTGRES_PORT` (default `55432`)
- `HOST_MINIO_API_PORT` (default `9900`)
- `HOST_MINIO_CONSOLE_PORT` (default `9901`)

To bootstrap backend services and start the API in one command:

```bash
./deploy-backend.sh
```

You must provide `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` (in shell env or `.env.deploy`).

`deploy-backend.sh` requires a **Java 21 JDK** on the server (not just a JRE). If compilation fails with `release version 21 not supported`, install JDK 21 and/or set `JAVA_HOME` explicitly.

`.env.deploy` is sourced with `set -a` so variables are **exported** for `docker compose` interpolation (important: compose does not read `.env.deploy` automatically).

If Postgres was first initialized with a different password than your current `.env.deploy`, you must either:
- rotate the DB user password inside Postgres to match, or
- recreate the Postgres volume (destructive): `docker compose -f compose.yaml down && docker volume rm araw-postgres-data` (this deletes DB data)

Operational helpers:

```bash
# stop the running backend process (uses .run/backend.pid)
./stop-backend.sh

# stop + deploy/start again
./restart-backend.sh
```

### API Overview

#### Articles (`/api/articles`)
- `POST /` – create draft/review/published articles.
- `PUT /{id}` – update content, tags, hero media association, and status.
- `PATCH /{id}/publish|review|archive` – transition lifecycle states.
- `GET /` – list with optional `status` filter (returns paged payload).
- `GET /{id}` / `GET /slug/{slug}` – fetch article by identifier or slug.

Publishing triggers `ArticlePublishedEvent`, which emits Gmail notifications after successful transactions.

#### Community Profiles (`/api/community/profiles`)
- `POST /` – onboard participants, volunteers, mentors, donors, etc.
- `PUT /{id}` – update bio, affiliations, and toggle featured/active flags.
- `PATCH /{id}/activate|deactivate|feature|unfeature` – quick status adjustments.
- `GET /` – search by `type`, `active`, and/or `featured`.
- `GET /{id}` / `GET /slug/{slug}` – retrieve profile details for rendering on the site.

#### Media (`/api/media`)
- `POST /` (multipart) – upload assets to MinIO with category tagging.
- `GET /{id}` – fetch metadata and a presigned download URL.
- `GET /{id}/url` – generate a presigned link only.
- `GET /?category=` – paginated listing filtered by `MediaCategory`.
- `DELETE /{id}` – remove object from MinIO and catalog.

#### Public Applications
- `POST /api/public/events/{applicationSlug}/applications` – open endpoint that (optionally) creates a participant, submits their application, and emails them using the Gmail templates. The event must be published, open for registration, and have capacity.
- `POST /api/public/events/id/{eventId}/applications` – same flow, but resolves the event directly by its UUID instead of the public slug.

#### Application Reviews (admin)
- `POST /api/admin/applications/{applicationId}/reviews` – record an admin review note for an application, including category-specific scores (e.g., interview, profile).
- `GET /api/admin/applications/{applicationId}/reviews` – list all review notes tied to an application for committee visibility.

### Authentication (OAuth 2.0)
- All write operations (POST/PUT/PATCH/DELETE) require a valid Google OAuth 2.0 ID token sent as `Authorization: Bearer <token>`.
- Tokens are validated against the Google issuer (`https://accounts.google.com`) and the configured client id (`spring.security.oauth2.client.registration.google.client-id`).
- Read-only GET requests remain public; Swagger UI and actuator endpoints are also open for diagnostics.
- Configure your Google OAuth client via `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` or the matching `application.yml` properties. The default redirect URI uses `{baseUrl}/login/oauth2/code/google`.
- Responses use bearer-token 401/403 semantics suitable for single-page apps; sessions remain stateless.

### Testing
```bash
mvn test
```
Coverage includes service-level specifications for article slug uniqueness, publication events, and community profile search semantics.

### Next Steps
- Harden authN/authZ once administrative roles are finalized.
- Extend content search (full-text) and caching for high-traffic stories.
- Expand notification channels (Slack/WhatsApp) alongside email.
- Layer reminder and survey automations onto the new templated email service.
- Introduce moderation workflows for user-generated submissions.
