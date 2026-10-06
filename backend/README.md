# Quizora — Backend API

Backend for **Quizora**, a web & mobile learning platform where students prepare for external
examinations (BECE now, WASSCE later) by solving past questions with timers, analytics and
institutional monitoring.

This repository implements **Phase 1 (MVP)** of the project brief: JHS/BECE objective-section
questions with authentication, courses, question bank, quiz engine, subscriptions, institutional
licensing and reporting.

> Working name is *Quizora* (from the project folder). Rename freely — the brief suggests *PassQ*
> as an alternative.

---

## 1. Tech stack

| Layer      | Choice |
|------------|--------|
| Language   | Java 17+ (runs on JDK 17–25) |
| Framework  | Spring Boot 3.5 (Web, Data JPA, Security, Validation) |
| Database   | PostgreSQL (default) · H2 in-memory for the `demo` profile & tests |
| Auth       | JWT (HS256, jjwt) with role-based access control |
| Reports    | CSV (built-in) + PDF (OpenPDF) |
| Build      | Maven (wrapper included — no Maven install needed) |

---

## 2. Quick start

### Option A — zero setup (demo profile, in-memory H2, auto-seeded)

```bash
# macOS / Linux / Git Bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo

# Windows CMD / PowerShell
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

API is live at `http://localhost:8080`.

### Option B — PostgreSQL (production-like)

Set a database password, JWT signing secret (at least 32 characters), and the exact
frontend origin before starting the database and API. For PowerShell:

```powershell
$env:DB_PASSWORD = "use-a-unique-local-password"
$env:JWT_SECRET = "replace-with-at-least-32-random-characters"
$env:CORS_ORIGINS = "http://localhost:3000"
```

```bash
docker compose up -d          # PostgreSQL is bound to localhost only
./mvnw spring-boot:run        # demo seeding is disabled outside the demo profile
```

Without Docker, install PostgreSQL, create a database named `quizora`, then configure via env vars:

| Env var       | Default                                      | Purpose |
|---------------|----------------------------------------------|---------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/quizora`   | JDBC URL |
| `DB_USER`     | `postgres`                                   | DB user |
| `DB_PASSWORD` | required                                     | Database and Compose password |
| `JWT_SECRET`  | required (at least 32 characters)           | JWT signing key |
| `CORS_ORIGINS`| required                                     | Exact comma-separated allowed origins |

The default profile uses Flyway migrations and does not create demo accounts. Keep secrets
out of source control; use environment variables or a secret manager.

### Run the tests

```bash
./mvnw test
```

End-to-end API tests cover: registration → courses → quiz → grading → progress → dashboard,
institutional onboarding, licence seats, and CSV/PDF report exports.

---

## 3. Seeded demo accounts

Seeded automatically only with the `demo` profile (`quizora.seed.enabled=true`). Demo mode
generates a temporary JWT signing key in memory; tokens stop working when the app restarts:

> Demo accounts use fixed sample passwords and are for local exploration only. Never enable
> demo seeding on a public or production deployment.

| Role | Email | Password | Notes |
|------|-------|----------|-------|
| Platform admin | `admin@quizora.app` | `Admin@123` | full admin panel access |
| Student | `student@demo.com` | `Student@123` | yearly subscription pre-activated |
| Institution admin | `school@demo.com` | `School@123` | "Demo JHS Accra" |
| Institution student | `kofi@demo.com` | `Student@123` | onboarded via licence code |

**Demo licence code:** `DEMO-CODE-1234` — 20 seats, valid 2026-01-01 → 2027-12-31.

Seed data also includes 6 JHS subjects and 44 BECE-style objective questions (2023 & 2024 papers).

---

## 4. Features implemented (Phase 1 MVP)

### A. User management & subscription module
- Roles: `STUDENT`, `INSTITUTION_ADMIN`, `ADMIN` (JWT + BCrypt).
- **Institutional licensing**: an institution buys a bulk licence (e.g. 20 seats); the API generates a
  unique code (`QZRA-XXXX-XXXX`); students register or link their account with the code; seats are
  counted and the licence has a validity window.
- **Individual access**: register → 7-day free trial → subscribe (`MONTHLY`/`YEARLY`).
  Payment is **mocked** (`POST /api/subscriptions/subscribe`) — swap in Paystack/Mobile Money later.
- Subscriptions expire lazily on read + via a nightly scheduled sweep.

### B. Student dashboard
- Personalised greeting (time of day), **daily motivational quote** (rotates every 24 h, deterministic),
- Analytics widget: courses attempted, questions attempted, accuracy, average score, total time, study streak.

### C. Courses & examination module
- Courses page with question counts, **year selection** per course,
- **Quiz engine**:
  - one-question-at-a-time (`GET .../questions/{index}`) — correct answers are **never** sent before submission;
  - optional **timed mode** — server-authoritative clock, `remainingSeconds` in every question,
    auto-grade on expiry;
  - answers graded server-side; full review (correct answer + explanation) returned on submit;
  - scientific calculator is a **client-side** component (web/mobile) — not part of the backend.

### D. Progress tracking & analytics
- Progress page: totals, accuracy, per-course breakdown, daily performance trend, attempt history.
- **Institutional reporting**: per-student snapshots + downloadable **CSV and PDF** reports.

### Admin panel API
- Course creation, single + **bulk question upload** (JSON), question listing/deletion,
- User listing, manual subscription activation, institution creation, platform stats.

---

## 5. API reference

Base URL: `http://localhost:8080` — send `Authorization: Bearer <token>` unless marked *public*.

### Auth
| Method | Path | Who | Description |
|--------|------|-----|-------------|
| POST | `/api/auth/register` | public | individual sign-up (grants trial) |
| POST | `/api/auth/register-institutional` | public | sign-up with licence code |
| POST | `/api/auth/login` | public | returns JWT + profile |
| POST | `/api/auth/join-code` | student | link account to an institution |
| GET | `/api/auth/me` | any | current profile + subscription |

### Subscriptions
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/subscriptions/me` | current subscription |
| POST | `/api/subscriptions/subscribe` | activate `{"plan":"MONTHLY"\|"YEARLY"}` (mock payment) |

### Courses
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/courses` | subjects with `questionCount` + available `years` |
| GET | `/api/courses/{id}` | course detail |
| GET | `/api/courses/{id}/years` | year selection (with per-year question counts) |

### Quizzes
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/quizzes/start` | `{"courseId":1,"year":2023,"timed":true,"timeLimitSeconds":600}` |
| GET | `/api/quizzes` | my attempt history (`?page=0&size=20`) |
| GET | `/api/quizzes/{id}` | attempt metadata (resume) |
| GET | `/api/quizzes/{id}/questions/{index}` | one question, **no correct answer** |
| POST | `/api/quizzes/{id}/answers` | `{"questionId":5,"selectedOption":"B","timeSpentSeconds":30}` |
| POST | `/api/quizzes/{id}/submit` | grades and closes (idempotent) |
| GET | `/api/quizzes/{id}/result` | graded result + full review |

### Dashboard & progress
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard` | greeting, daily quote, analytics, subscription |
| GET | `/api/progress` | totals, per-course, trend, recent attempts |
| GET | `/api/progress/attempts` | paginated history |

### Institutions (`INSTITUTION_ADMIN` or `ADMIN`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/institutions` | admin: create institution + admin account |
| GET | `/api/institutions/{id}` | overview incl. licence codes & seat usage |
| POST | `/api/institutions/{id}/licenses` | generate licence `{"totalSeats":20,"validFrom":"...","validUntil":"..."}` |
| GET | `/api/institutions/{id}/students` | students + progress snapshots |
| GET | `/api/institutions/{id}/report/summary` | reporting dashboard data |
| GET | `/api/institutions/{id}/report/export?format=csv\|pdf` | downloadable report |

### Admin (`ADMIN` only)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/stats` | platform statistics |
| POST | `/api/admin/courses` | create course |
| POST | `/api/admin/questions` | add one question |
| POST | `/api/admin/questions/bulk` | upload a full paper |
| GET | `/api/admin/questions?courseId=&year=` | list (incl. correct answers) |
| DELETE | `/api/admin/questions/{id}` | delete question |
| GET | `/api/admin/users?role=` | list users |
| POST | `/api/admin/subscriptions/activate` | manually activate/extend a subscription |
| POST | `/api/admin/institutions` | create institution |

Errors return JSON: `{"status":400,"error":"Bad Request","message":"...","path":"...","timestamp":"..."}`.

---

## 6. Project structure

```
src/main/java/com/quizora/backend/
├── QuizoraApplication.java     # entry point (+ @EnableScheduling)
├── config/                     # SecurityConfig, DataSeeder
├── security/                   # JWT service/filter, principal, CurrentUser
├── domain/                     # JPA entities + enums
├── repository/                 # Spring Data JPA repositories
├── dto/                        # request/response records
├── service/                    # business logic
├── controller/                 # REST endpoints
└── exception/                  # global error handling
src/test/java/...               # integration + unit tests
```

---

## 7. Roadmap (later phases)

- **Phase 2** — Section B (subjective): the `QuestionSection.SUBJECTIVE` enum already exists; add
  model answers, marking schemes and grading logic.
- **Phase 3** — WASSCE: `Level.SHS` already exists on courses; extend the seed/admin flows.
- **Payments** — replace the mock activation in `SubscriptionService.activate()` with Paystack /
  Mobile Money payment verification (keep the same subscription lifecycle).
- **Production** — Flyway migrations, real JWT secret rotation, rate limiting, refresh tokens.
