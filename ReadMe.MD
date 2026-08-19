# MediClinic

MediClinic is a multi-module **Java EE (Jakarta EE 7)** web application for managing a small clinic's day-to-day operations — patient registration, appointment booking, doctor scheduling, consultations, prescriptions, billing/invoicing, and online payments via **eSewa** and **Khalti** (Nepali payment gateways).

The app is built as a classic EAR deployment (EJB + WAR packaged together) and uses **JSF (Facelets) + PrimeFaces** for the UI, **Hibernate/JPA** for persistence, and **MySQL** as the database.

---

## Features

### Role-based access
Four roles are seeded automatically on first startup (`RoleSeeder`): **ADMIN**, **DOCTOR**, **RECEPTIONIST**, **PATIENT**. Each role has its own set of pages under `MediClinic-web/src/main/webapp/{admin,doctor,patient,receptionist}`.

| Role | Capabilities |
|---|---|
| **Admin** | Manage staff accounts, view/manage all appointments, invoices, doctor schedules, patients, system reports, and server logs |
| **Doctor** | View their schedule and appointments, run consultations, view patient history, manage their profile |
| **Receptionist** | Book appointments on behalf of patients, manage patient records, manage invoices |
| **Patient** | Book/view appointments, view prescriptions, view billing/invoices, manage profile & photo |

### Core modules
- **Authentication** — registration with email verification, login, forgot/reset password (token-based, `TokenUtil`)
- **Appointments** — request → approve → complete/cancel/reschedule workflow (`Appointment.Status`: `REQUESTED`, `APPROVED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`)
- **Doctor scheduling** — weekly availability management (`DoctorSchedule`)
- **Consultations** — doctor notes/diagnosis tied to an appointment
- **Prescriptions** — issued from a consultation, viewable by the patient
- **Billing & Invoices** — invoice generation and status tracking, downloadable via `InvoiceServlet`
- **Online payments** — integrated with **eSewa** and **Khalti** sandbox/test APIs, with initiate + callback servlets
- **Medical records & profile photos** — file upload/download via `MedicalRecordServlet` and `PhotoServlet`
- **Reports** — admin-facing analytics (`ReportsBean`, `ReportService`)
- **Activity & server logs** — audit trail of user/system actions (`ActivityLogDao`, `ServerLogsBean`)

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java (Jakarta/Java EE 7 API) |
| Web framework | JSF (Facelets) + PrimeFaces |
| Business logic | EJB (stateless session beans / `@Singleton`) |
| Persistence | JPA 2.1 via Hibernate (`hibernate-entitymanager` 4.3.1) |
| Database | MySQL (JTA datasource `jdbc/mediclinicdb`) |
| Build | Maven (multi-module: `ejb`, `web`, `ear`) |
| App server | GlassFish / Payara (Java EE 7 compatible) |
| Email | JavaMail (SMTP, configured for Gmail) |
| Payments | eSewa ePay v2 (sandbox), Khalti ePayment v2 (sandbox) |

---

## Project structure

```
MediClinic/
├── pom.xml                     # Parent POM
├── MediClinic-ejb/             # Business logic module (packaging: ejb)
│   └── src/main/java/
│       ├── entity/             # JPA entities (User, Patient, Doctor, Appointment, ...)
│       ├── dao/                # Data access objects
│       ├── service/             # Business services (AppointmentService, BillingService, ...)
│       ├── util/                # TokenUtil, PasswordUtil, EsewaConfig, KhaltiConfig
│       └── startup/             # RoleSeeder (seeds default roles on boot)
├── MediClinic-web/             # Web module (packaging: war)
│   └── src/main/
│       ├── java/
│       │   ├── com/mycompany/   # JSF managed beans (per role: Admin*, Doctor*, Patient*, Receptionist*)
│       │   ├── filter/          # AuthFilter (role/session guarding)
│       │   ├── servlet/         # Payment callbacks, invoice/photo/medical-record download
│       │   └── util/            # JacksonConfig, PhotoStorage, LoggedInUser
│       └── webapp/
│           ├── admin/ doctor/ patient/ receptionist/   # Role-specific XHTML pages
│           ├── error/           # 403/404/500 pages
│           └── WEB-INF/         # web.xml, beans.xml
└── MediClinic-ear/              # EAR packaging module — bundles ejb + web for deployment
```

---

## Prerequisites

- **JDK 8** (Java EE 7 / Hibernate 4.3.1 target this baseline)
- **Maven 3.x**
- **MySQL** (5.x/8.x) running locally or remotely
- A **Java EE 7–compatible application server** (e.g. GlassFish 4/5 or Payara Server) with:
  - A JTA datasource bound to JNDI name `jdbc/mediclinicdb`
  - A JavaMail session, or the app's own SMTP config (see below)
- (Optional) eSewa and Khalti **sandbox/test** merchant credentials if you want to exercise the payment flows

---

## Configuration

### 1. Database
Create a MySQL database (e.g. `mediclinicdb`) and configure a JTA-enabled datasource on your app server pointing to it, bound as `jdbc/mediclinicdb`. Schema is created/updated automatically on deploy via:

```
MediClinic-ejb/src/main/resources/META-INF/persistence.xml
# hibernate.hbm2ddl.auto = update
```

### 2. Mail (SMTP)
Edit `MediClinic-ejb/src/main/resources/mail.properties`:

```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.from=<your-email>
mail.username=<your-email>
mail.password=<your-app-password>
app.base.url=http://localhost:8080/MediClinic-web
```


### 3. Payment gateways
Sandbox credentials live in:
- `MediClinic-ejb/src/main/java/util/EsewaConfig.java`
- `MediClinic-ejb/src/main/java/util/KhaltiConfig.java`

These currently point at eSewa's and Khalti's **test/sandbox** endpoints with test keys. Swap in your own merchant credentials (and production URLs) before going live — and, as above, move secrets out of source-controlled `.java` files.

---

## Build & run

```bash
# From the project root (where the parent pom.xml lives)
mvn clean install

# Deploy the resulting EAR to your app server
# MediClinic-ear/target/MediClinic-ear-1.0-SNAPSHOT.ear
```

Then deploy the EAR to GlassFish/Payara (via the admin console, `asadmin deploy`, or your IDE's server integration — the project has NetBeans metadata, so opening it directly in NetBeans with a registered GlassFish server is the easiest path).

Once deployed, the app is reachable at:

```
http://localhost:8080/MediClinic-web/
```

The welcome/landing page is `login.xhtml`. On first boot, `RoleSeeder` creates the four roles automatically; you'll still need to register a user and (for admin access) promote that user's role directly in the database or via an admin bootstrap step.

---

## Known limitations / ideas for improvement

- No automated test suite yet (`MediClinic-web/src/test` is currently empty)
- No REST API layer — UI and business logic are coupled through JSF managed beans
- Payment gateway keys and SMTP credentials are hardcoded rather than externalized
- No lab/pharmacy/inventory modules
- No in-app notifications or SMS reminders (email service exists and could be extended)
- No dashboard charts/visual analytics (data exists via `ReportService`/`ReportsBean`)

---

