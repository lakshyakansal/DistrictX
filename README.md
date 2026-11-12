# DistrictX — discover your city

A District-inspired, independently branded discovery and booking MVP built with **React + Vite**, **Java 21 + Spring Boot 3**, and **PostgreSQL**.

> DistrictX is an original implementation and visual direction. It does not use District's source code, private APIs, branding assets, or proprietary data.

## What works in this starter

### Customer website
- Responsive landing page and discovery catalogue.
- Category browsing and text search.
- Experience detail pages.
- Registration and login with BCrypt password hashing and JWT sessions.
- Authenticated booking creation with stock checks and server-side total calculation.
- Booking history and customer profile.
- Wishlist saved locally in the browser.
- Demo seed catalogue for events, movies, dining and sports.

### Admin
- Admin login and protected admin API.
- Overview metrics for users, catalogue entries, bookings and gross booking value.
- Create and soft-delete catalogue experiences.
- Role-based API access.

### API
- REST endpoints and validation.
- PostgreSQL persistence using Spring Data JPA.
- H2 can be used for a local smoke test by overriding the datasource.
- JWT bearer authentication.
- Booking stock decrement runs transactionally.

## Important integration boundaries

This is a runnable full-stack foundation, not a live District supplier network. The seeded inventory is demo content. The current booking endpoint creates a confirmed **demo booking** and does not charge a card. Before accepting real money, connect a Razorpay account and implement order creation, server-side signature verification, webhook reconciliation, refunds and payment idempotency. Live movie showtimes, restaurant table inventory, venue slot calendars, maps, email/SMS, QR check-in and third-party ticket suppliers require authorized provider credentials and are not fabricated here.

The category catalogue is shared across experiences; category-specific workflows (cinema seat maps, dining table/time-slot reservations, sports court calendars, organizer settlement, QR scanning and refund operations) are clear next implementation modules rather than falsely represented as live integrations.

## Repository structure

```text
DistrictX/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/districtx/
│       │   ├── DistrictXApplication.java
│       │   ├── Domain.java
│       │   ├── Repositories.java
│       │   ├── SecurityConfig.java
│       │   └── ApiController.java
│       └── resources/application.properties
├── frontend/
│   ├── package.json
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       └── styles.css
├── docker-compose.yml
└── README.md
```

## Run locally

### Requirements
- Java 21
- Maven 3.9+
- Node.js 20+
- PostgreSQL 15+ (or Docker)

### 1. Start PostgreSQL

From the project root:

```bash
docker compose up -d db
```

### 2. Start backend

```bash
cd backend
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`

The backend creates the tables and seeds demo experiences on first startup.

**Demo admin account**
- Email: `admin@districtx.local`
- Password: `Admin@12345`

Change this password and set a strong `JWT_SECRET` before any shared or public deployment.

### 3. Start frontend

In another terminal:

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173`.

## Main API routes

| Method | Route | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create customer |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| GET | `/api/experiences` | Public | List/filter catalogue |
| GET | `/api/experiences/{id}` | Public | Experience detail |
| POST | `/api/bookings` | Customer | Create demo booking |
| GET | `/api/bookings/me` | Customer | Current user's bookings |
| GET | `/api/admin/overview` | Admin | Dashboard metrics |
| POST | `/api/admin/experiences` | Admin | Create listing |
| PUT | `/api/admin/experiences/{id}` | Admin | Update listing |
| DELETE | `/api/admin/experiences/{id}` | Admin | Soft-delete listing |

Query examples:
- `/api/experiences?category=Events`
- `/api/experiences?q=comedy`

## Environment variables

### Backend

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP port |
| `DATABASE_URL` | local PostgreSQL JDBC URL | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | local development value | HMAC signing secret (32+ chars) |
| `FRONTEND_URL` | `http://localhost:5173` | Frontend origin reference |

For Render PostgreSQL, use the **internal database URL** and ensure it is a JDBC URL beginning `jdbc:postgresql://`. If Render gives a `postgres://` URL, convert the scheme to `jdbc:postgresql://` and configure username/password separately when needed.

### Frontend

| Variable | Example |
|---|---|
| `VITE_API_URL` | `https://your-api.onrender.com/api` |

## Deploy

### Backend — Render
1. Push this repository to GitHub.
2. Create a Render PostgreSQL database.
3. Create a **Web Service** from the repository, root directory `backend`.
4. Build command: `mvn clean package -DskipTests`
5. Start command: `java -jar target/districtx-api-1.0.0.jar`
6. Add `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `FRONTEND_URL`.
7. Use a long random `JWT_SECRET`; do not deploy the demo secret.

### Frontend — Vercel
1. Import the same GitHub repository.
2. Set Root Directory to `frontend`.
3. Build command: `npm run build`.
4. Output directory: `dist`.
5. Set `VITE_API_URL` to the deployed backend URL plus `/api`.
6. Redeploy after changing environment variables.

For production, restrict CORS to the exact Vercel domain, configure HTTPS, add database backups, rate limiting, structured logs, monitoring, and secret rotation.

## Production hardening checklist

- [ ] Replace demo admin credentials and create an admin bootstrap process.
- [ ] Add Razorpay order creation, signature verification and webhooks.
- [ ] Add booking holds with expiry and safe concurrent inventory reservation.
- [ ] Add cancellation/refund policy and payment reconciliation.
- [ ] Add category-specific seat/table/slot inventory.
- [ ] Add QR ticket issuance and server-side scan validation.
- [ ] Add organizer accounts, ownership checks and payout/settlement logic.
- [ ] Add email/SMS notifications and customer support workflows.
- [ ] Add pagination, rate limiting, audit logs and automated tests.
- [ ] Add accessibility review and end-to-end checkout tests.

## License

Use this starter as a base for your own product. Add your own brand assets, legal pages, supplier agreements and privacy policy before public launch.
