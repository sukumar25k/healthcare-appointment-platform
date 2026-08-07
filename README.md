# Healthcare Appointment Management Platform

A full-stack appointment booking system: Spring Boot + JWT auth backend,
React (Vite) frontend, automated backend tests, and Playwright E2E tests.

## Tech Stack

| Layer      | Technology                                         |
|------------|-----------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.3.4, Spring Security, Spring Data JPA |
| Database   | H2 (in-memory, default) or PostgreSQL              |
| Auth       | JWT (jjwt)                                          |
| API Docs   | springdoc-openapi (Swagger UI)                      |
| Frontend   | React 18 + Vite, React Router, Axios                |
| Testing    | JUnit 5 + Mockito (backend), Playwright (E2E)       |

## Project Structure

```
healthcare-appointment-platform/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/healthcare/appointment/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── application-postgres.yml
│   ├── src/test/java/...
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                   # React + Vite application
│   ├── src/
│   │   ├── api/
│   │   ├── context/
│   │   ├── components/
│   │   └── pages/
│   ├── e2e/
│   ├── playwright.config.js
│   ├── Dockerfile
│   └── package.json
│
├── screenshots/                # Project screenshots
│   ├── 01-home.png
│   ├── 02-login.png
│   ├── 03-register.png
│   ├── 04-dashboard.png
│   ├── 06-book-appointment.png
│   ├── 07-appointment-history.png
│   ├── 08-admin-dashboard.png
│   └── 09-swagger-api.png
│
├── docs/
│   └── database-schema.md
│
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── README.md
└── .gitignore
```

## Prerequisites

- Java 17+
- Maven 3.8+ (or use the included `mvnw` if you add one)
- Node.js 18+ and npm
- Docker & Docker Compose (optional, for containerized run)

> **Note on this deliverable:** the code was written and (for the frontend)
> built/verified in a sandboxed environment without access to Maven Central.
> The backend code was written carefully against standard, well-documented
> Spring Boot 3.3.x / jjwt 0.12.x / springdoc 2.6.x APIs, but you should run
> `mvn clean verify` yourself on first checkout to confirm it builds and all
> tests pass in your environment.

## Running the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. It uses an in-memory H2
database by default — no setup required. Data resets on every restart
except a small set of demo appointment slots that gets seeded automatically
so "Fetch Available Slots" has data to show immediately.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console
  (JDBC URL: `jdbc:h2:mem:appointmentdb`, user `sa`, empty password)

### Using PostgreSQL instead of H2

```bash
# Start a local Postgres, then:
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/appointmentdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Running backend tests

```bash
cd backend
mvn test          # unit + integration tests (uses an isolated H2 test DB)
```

Test coverage includes:
- User registration & login (success + duplicate email + wrong password)
- Appointment booking & cancellation (full workflow)
- Duplicate booking prevention (two users racing for one slot)
- Available slots (auth-gated fetch)
- Authentication & authorization (401 for no/bad token, 403 for cancelling someone else's appointment)
- Validation & error handling (missing fields, invalid email, short password, 404s)
- **Concurrent booking**: a dedicated test (`ConcurrentBookingTest`) fires two
  simultaneous booking requests at the same slot and asserts exactly one
  succeeds.

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

Opens on **http://localhost:5173**. It's configured to call the backend at
`http://localhost:8080/api` (see `.env.development`); a Vite dev-server
proxy is also configured for `/api`.

Build for production:

```bash
npm run build     # outputs to frontend/dist
```

### UI overview

- **Register / Login** — creates an account or authenticates; JWT is stored
  in `localStorage` and attached to every subsequent API call.
- **Available Slots** — lists open appointment slots with doctor, department,
  date/time; "Book Appointment" triggers the booking flow with the status
  messages specified in the assignment:
  - `Fetching available slots...`
  - `Booking appointment...`
  - `Appointment booked successfully`
  - `Slot already booked`
- **Appointment History** — lists all of the user's appointments (booked and
  cancelled), with a cancel action for active bookings.

## Running E2E Tests (Playwright)

Playwright's config starts the Vite dev server automatically, but the
**backend must already be running** on port 8080 since the frontend talks to
a real API.

```bash
# Terminal 1
cd backend && mvn spring-boot:run

# Terminal 2
cd frontend
npx playwright install     # first time only, downloads browsers
npm run test:e2e
```
## 📸 Application Screenshots


### Home Page
![Home Page](screenshots/home%20page.png)

### Register Page
![Register Page](screenshots/register%20page.png)

### Login Page
![Login Page](screenshots/signin%20page.png)

### Appointment History
![Appointment History](screenshots/appointment%20history.png)

### Swagger API
![Swagger API](screenshots/Swagger%20API%20Documentation.png)
Other useful commands:

```bash
npm run test:e2e:headed    # watch the browser while tests run
npm run test:e2e:ui        # interactive Playwright UI mode
npm run test:e2e:debug     # step through tests
```

E2E coverage:
- User registration/login
- Book appointment
- Cancel appointment
- View appointment history
- View available slots
- Unauthenticated redirect to login

## Running Everything with Docker Compose

```bash
docker compose up --build
```

- Backend: http://localhost:8080
- Frontend: http://localhost:3000
- Postgres: localhost:5432 (available if you switch the backend to the
  `postgres` profile; the backend container uses H2 by default — see
  `docker-compose.yml` comments to switch)

## API Summary

| Method | Endpoint                        | Auth required | Description                     |
|--------|----------------------------------|----------------|----------------------------------|
| POST   | `/api/auth/register`             | No             | Register a new user              |
| POST   | `/api/auth/login`                | No             | Login, returns JWT               |
| GET    | `/api/slots/available`           | Yes            | Fetch available appointment slots|
| POST   | `/api/appointments`              | Yes            | Book an appointment (`{slotId}`) |
| PATCH  | `/api/appointments/{id}/cancel`  | Yes            | Cancel an appointment            |
| GET    | `/api/appointments`              | Yes            | Fetch the user's appointment history |

Authenticated requests need `Authorization: Bearer <token>`.

Full request/response schemas are in Swagger UI once the backend is running.

## Database Schema

See [`docs/database-schema.md`](docs/database-schema.md) for full table
definitions and the concurrency-safety design.

## Security & Architecture Notes

- Passwords are hashed with BCrypt, never stored or returned in plaintext.
- JWT is stateless (`SessionCreationPolicy.STATELESS`); no server-side session.
- All input is validated with Bean Validation (`@Valid` + `@NotBlank`,
  `@Email`, `@Size`, etc.); validation failures return 400 with a
  field-by-field error map.
- A single `GlobalExceptionHandler` converts every exception into a
  consistent JSON error shape with the right HTTP status.
- A user can only cancel their own appointments (403 otherwise).
- Layering is Controller → Service → Repository, with DTOs at the API
  boundary so entities are never exposed directly over HTTP.

## Known Limitations / Notes

- This is a demo-scale project (per the assignment's scope): the "doctor"
  concept is a plain string on `Slot`, not a full user/entity with its own
  login — only patient users register/login, matching the assignment's
  "Register User / Login User" requirement.
- Available slots are seeded on startup (H2 profile only) purely so the UI
  has data; this seeding is not one of the required APIs and adds no new
  endpoints.
- No repo push was performed as part of this deliverable — the project is
  provided as a ready-to-push folder; run `git init && git add . && git commit`
  and push to a GitHub repo of your choice.
