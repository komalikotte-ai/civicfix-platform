# CivicFix Platform

> **Production-Oriented Municipal Grievance & City Operations Management Platform**

CivicFix is a multi-tenant municipal operations and grievance redressal platform engineered to scale across city administrations, municipal departments, wards, field crews, and citizens.

---

## 1. Shared Technology Stack

| Layer | Technology | Primary Role |
| :--- | :--- | :--- |
| **Main Backend** | **Java 17+ / Spring Boot 3.3.x** | Core business domain, REST APIs, workflow state machine, SLA engine, security & RBAC |
| **Main Frontend** | **React 18 / Vite** | Citizen portal, municipal operations dashboard, workflow management |
| **Database** | **MySQL 8.0+** | Relational transactional persistence, ACID compliance, spatial data support |
| **AI Microservice**| **Python 3.11+ / FastAPI** | Grievance categorization, automated routing, duplicate report detection, urgency scoring |
| **Security** | **Spring Security 6 + JJWT** | Stateless JWT authentication, role-based access control (RBAC), audit trail |
| **API Architecture**| **RESTful JSON / `/api/v1/`** | Standardized response envelope, centralized error format, pagination |

---

## 2. Shared Project Architecture

The repository is organized into three decoupled, production-oriented applications:

```text
civicfix-platform/
├── backend/                   # Spring Boot 3.3 REST API Service
│   ├── src/main/java/com/civicfix/platform/
│   │   ├── auth/              # Authentication controller, login/register DTOs, AuthService
│   │   ├── users/             # User, Role entities, RBAC enums, UserRepository, UserService
│   │   ├── organizations/     # Municipal corporation entities, DTOs, controllers, services
│   │   ├── departments/       # Municipal departments (Sanitation, Roads, Electricity, etc.)
│   │   ├── wards/             # Administrative zones and geographic ward boundaries
│   │   ├── complaints/        # Grievance lifecycle, priority, category, tracking numbers
│   │   ├── workflow/          # Finite State Machine (FSM) transition validator & audit history
│   │   ├── sla/               # Department & priority-driven resolution target deadlines
│   │   ├── security/          # JWT provider, filter, entry points, UserPrincipal, SecurityConfig
│   │   ├── audit/             # Immutable audit log ledger for all system mutations
│   │   └── common/            # Shared envelopes, GlobalExceptionHandler, JPA auditing
│   ├── src/main/resources/
│   │   ├── application.yml    # Base configuration with environment variable interpolation
│   │   ├── application-dev.yml# Development profile with SQL logging
│   │   ├── application-prod.yml# Production profile with schema validation
│   │   └── db/migration/V1__initial_schema.sql # Canonical MySQL schema baseline
│   ├── src/test/              # JUnit 5 & Spring Boot test suite with in-memory H2 profile
│   └── pom.xml                # Maven build definition
│
├── frontend/                  # React + Vite Single Page Application (SPA)
│   ├── src/
│   │   ├── app/               # Root App router, navigation configuration
│   │   ├── components/
│   │   │   ├── common/        # Button, Input, Select, Card, Badge, Spinner, Alert, Pagination
│   │   │   └── layout/        # AppLayout, Navbar, Sidebar with role-aware navigation
│   │   ├── features/
│   │   │   ├── auth/          # AuthContext, ProtectedRoute, LoginPage, RegisterPage
│   │   │   ├── complaints/    # ComplaintsListPage, ComplaintDetailPage, CreateComplaintPage
│   │   │   └── dashboard/     # Metric cards, operations summary, recent activity
│   │   ├── services/          # Centralized Axios apiClient, tokenService, domain API clients
│   │   ├── hooks/             # useAuth, usePagination, useApi
│   │   ├── i18n/              # English & Hindi dictionaries and translation helpers
│   │   └── tests/             # Node unit test suite (tokenService, apiClient contracts)
│   ├── vite.config.js         # Dev proxy to backend (/api -> http://localhost:8080)
│   └── package.json           # React dependencies and scripts
│
├── ai-service/                # Python FastAPI Microservice
│   ├── app/
│   │   ├── api/v1/            # Versioned AI routes (/categorize, /check-duplicates, /analyze-urgency)
│   │   ├── schemas/           # Pydantic request/response validation schemas
│   │   ├── services/          # NLP classifier & keyword extractor algorithms
│   │   ├── config.py          # Pydantic settings loading from environment
│   │   └── main.py            # FastAPI entry point, CORS middleware, healthcheck
│   ├── tests/                 # Pytest healthcheck and endpoint tests
│   ├── requirements.txt       # Python dependencies
│   └── Dockerfile             # Container deployment definition
│
├── docker-compose.yml         # Local multi-service development orchestration
├── .env.example               # Root environment variable documentation template
└── README.md                  # System architecture and team blueprint
```

---

## 3. Configuration & Environment Variable Strategy

All environment variables follow a strict fallback convention: **local defaults work out-of-the-box in development**, while container and cloud deployments inject overrides securely.

### Key Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Spring Boot HTTP listening port |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (`dev`, `prod`, `test`) |
| `DB_HOST` | `localhost` | MySQL hostname or Docker service name |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `civicfix_db` | Main relational database name |
| `DB_USER` | `civicfix_user` | MySQL database username |
| `DB_PASSWORD` | `civicfix_pass` | MySQL database password |
| `JWT_SECRET` | *(32+ char key)* | HMAC-SHA256 signature key for JWT tokens |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Access token validity duration in milliseconds |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Allowed web clients |
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Backend endpoint for frontend client |
| `AI_SERVICE_URL` | `http://localhost:8000` | FastAPI service URL for backend calls |

---

## 4. Database Configuration & Schema Baseline

The relational schema is configured in `backend/src/main/resources/db/migration/V1__initial_schema.sql` and auto-managed via Spring Data JPA.

### Core Tables
1. **`organizations`**: Municipal Corporations / Urban Local Bodies.
2. **`wards`**: Geographical administrative units linked to organizations.
3. **`departments`**: Administrative domains (Sanitation, Roads, Electricity, Water).
4. **`roles`**: RBAC permissions (`ROLE_CITIZEN`, `ROLE_OPERATOR`, `ROLE_OFFICER`, `ROLE_DEPT_ADMIN`, `ROLE_SUPER_ADMIN`).
5. **`users`**: Platform users with bcrypt password hash, ward, department, and role relationships.
6. **`user_roles`**: Many-to-many junction table between users and roles.
7. **`complaints`**: Core grievance records with tracking numbers (`CF-YYYYMMDD-XXXX`), status, SLA target, location coordinates, and assignment.
8. **`workflow_transitions`**: Immutable audit log of every status change (from state, to state, remarks, actor).
9. **`sla_policies`**: Target resolution times in hours by department and priority.
10. **`audit_logs`**: Platform-wide operational mutation ledger.

---

## 5. API Conventions & Contracts

### Base Path & Versioning
All REST APIs are strictly versioned under `/api/v1/`:
- Auth: `/api/v1/auth/**`
- Users: `/api/v1/users/**`
- Organizations: `/api/v1/organizations/**`
- Departments: `/api/v1/departments/**`
- Wards: `/api/v1/wards/**`
- Complaints: `/api/v1/complaints/**`
- Workflow: `/api/v1/workflow/**`
- SLA: `/api/v1/sla/**`
- Audit Logs: `/api/v1/audit-logs/**`
- AI Microservice: `/api/v1/ai/**`

### Standard Response Envelope (`ApiResponse<T>`)
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-28T05:30:00Z"
}
```

### Standard Error Envelope (`ApiErrorResponse`)
```json
{
  "success": false,
  "statusCode": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed for one or more fields",
  "path": "/api/v1/complaints",
  "validationErrors": [
    {
      "field": "title",
      "rejectedValue": "bad",
      "message": "Title must be between 5 and 200 characters"
    }
  ],
  "timestamp": "2026-10-28T05:30:00Z"
}
```

### Pagination Convention (`PagedResponse<T>`)
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

Query parameters:
- `page`: 0-indexed page number (default: `0`)
- `size`: Items per page (default: `20`, max: `100`)
- `sortBy`: Field name to sort by (default: `createdAt`)
- `sortDirection`: `asc` or `desc` (default: `desc`)

---

## 6. Frontend Architecture & API Client Convention

- **`frontend/src/services/apiClient.js`**:
  - Centralized Axios instance with base URL `/api/v1`.
  - **Request Interceptor**: Extracts JWT from `tokenService` and adds `Authorization: Bearer <token>`.
  - **Response Interceptor**: Automatically unwraps backend `response.data.data` payload.
  - **Global Error Handling**: Translates 401 Unauthorized responses to automatic session invalidation and clean redirects to `/login`.
- **`frontend/src/features/auth/AuthContext.jsx`**:
  - Provides reactive auth state (`user`, `isAuthenticated`, `login`, `register`, `logout`, `hasRole`).
  - Supports role-based protection with `<ProtectedRoute allowedRoles={['SUPER_ADMIN']} />`.

---

## 7. Cross-Member Responsibilities

| Team Member | Owned Domain & Functional Responsibilities |
| :--- | :--- |
| **Member 1** *(Current)* | **Authentication, Users, Organizations, Complaints, Workflow FSM, SLA Engine, Security & Core Backend** |
| **Member 2** | **Citizen Portal, Public Grievance Portal, Interactive Maps, Accessibility (A11y), and Translations (i18n)** |
| **Member 3** | **Field Operations, Work Orders, Municipal Assets, Scheduled Maintenance, Inventory, Warehouses, Vendors** |
| **Member 4** | **Analytics & BI Dashboards, Reports, AI Microservice Enhancements, SMS/Email Notifications, Finance & Observability** |

---

## 8. How Member 1 Should Continue

Member 1 holds ownership of the core backend and grievance lifecycle. Now that the shared architecture is established, Member 1 should proceed with the following specific tasks:

1. **Seed Master Data**:
   - Create a Spring Boot `CommandLineRunner` or SQL seed in `backend/src/main/resources/db/migration/` to populate default departments (Sanitation, Public Works, Water Supply, Electricity) and demo user accounts for each role (`admin`, `dept_admin`, `officer1`, `citizen1`).
2. **Deepen Workflow State Machine**:
   - Implement role-specific status transition gates in `ComplaintService.java` (e.g. only `ROLE_OFFICER` can mark `IN_PROGRESS -> RESOLVED`, only `ROLE_CITIZEN` or `ROLE_SUPER_ADMIN` can close or reopen).
3. **Automate SLA Calculation & Escalation Cron**:
   - Add a scheduled task (`@Scheduled(cron = "0 */15 * * * *")`) in the `sla` package that detects complaints whose `slaDueAt` has elapsed without resolution, automatically transitions them to `ESCALATED`, and logs the event in `audit_logs`.
4. **Expand Unit & Integration Tests**:
   - Add MockMvc test cases in `backend/src/test/java/` verifying `/api/v1/auth/login`, `/api/v1/complaints` validation rules, and unauthorized access denials.

---

## 9. Git Safety Rules

- **Do NOT run `git commit`.**
- **Do NOT run `git push`.**
- **Do NOT merge branches.**
- Work strictly inside the designated Git feature branch (`feature/member-1-core-backend`).
- All commits and pull requests are managed manually by the Git coordinator.
