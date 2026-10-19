# 🏋️ ApexFit - Gym Management System

A full-stack Gym Management Web Application built with **Spring Boot 3**, **Spring Security 6**, **Spring Data JPA**, **Thymeleaf**, **Bootstrap 5**, and an **in-memory H2 database**.

---

## 🌟 Key Features

1. **Role-Based Authentication & Portals (Spring Security)**:
   - **Admin Portal**: Complete administrative control over athletes, trainers, plans, subscriptions, and class timetables.
   - **Trainer / Coach Portal**: Dedicated dashboard displaying assigned classes, student rosters, personal training athletes, and attendance.
   - **Member / Athlete Portal**: Dedicated member hub tracking active subscription validity countdown, personal coach contacts, class reservations, and attendance logs.
   - Pre-configured demo accounts with one-click login autofill buttons.

2. **Fitness Class Scheduling & Booking**:
   - Schedule group workouts, CrossFit WODs, Yoga flows, and HIIT circuits with custom rooms, capacity, and recurring days.
   - Real-time occupancy indicators and capacity meters.
   - Athletes can book and cancel reservations with spot availability enforcement.
   - Coaches and Admins can view enrolled attendee rosters for every class.

3. **Interactive Operations Dashboard**:
   - Live metrics: Total members, Active athletes, Today's check-ins, Active plan subscriptions, and Total revenue.
   - Embedded front-desk quick check-in desk directly on the dashboard.
   - Early warning alert banner for subscriptions expiring within 7 days.
   - Real-time facility occupancy (currently working out vs. completed sessions).

4. **Member Management**:
   - Register, search, view, edit, and delete member profiles.
   - Assign personal trainers to members.
   - Emergency contact management.
   - Detailed individual member profiles showing full membership history and attendance timeline.

5. **Trainers & Coaches**:
   - Manage certified trainers, specializations (Bodybuilding, CrossFit, Yoga, HIIT), experience, and compensation.
   - Visual roster tracking how many members are assigned to each trainer.

6. **Membership Plans & Subscriptions**:
   - Create custom subscription packages (e.g., Monthly Starter, Quarterly Pro, Annual Elite, VIP Platinum).
   - Automated end-date calculation and payment tracking (`PAID`, `PENDING`).

7. **Attendance & Live Occupancy**:
   - Front desk check-in timestamp logging with optional workout notes.
   - One-click member checkout timestamp logging.
   - Complete historical attendance logging.

---

## 🔑 Demo Login Accounts

| Role | Username / Email | Password | Access / Landing Portal |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@apexfit.com` | `password123` | Full Admin Dashboard (`/dashboard`) |
| **Trainer** | `elena.rostova@apexfit.com` | `password123` | Coach Portal (`/trainer/dashboard`) |
| **Trainer** | `alex.hunter@apexfit.com` | `password123` | Coach Portal (`/trainer/dashboard`) |
| **Member** | `sophia.miller@example.com` | `password123` | Athlete Portal (`/member/portal`) |
| **Member** | `james.anderson@example.com`| `password123` | Athlete Portal (`/member/portal`) |

*(One-click fill buttons are available directly on the login page at [http://localhost:8081/login](http://localhost:8081/login))*

---

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.2.5 (Java 17)
  - Spring Web MVC
  - Spring Security 6 (BCrypt Password Hashing, Role-Based Access Control)
  - Spring Data JPA (Hibernate)
  - Jakarta Bean Validation
- **Frontend**:
  - Thymeleaf Template Engine + Thymeleaf Spring Security 6 dialect
  - Bootstrap 5.3 + Bootstrap Icons
  - Google Fonts (Plus Jakarta Sans)
- **Database**:
  - H2 Database Engine (In-Memory, Zero Setup)
- **Build Tool**:
  - Apache Maven 3.9+

---

## 🚀 How to Run the Application

### 1. Requirements
- Java 17+
- Maven 3.8+

### 2. Launch the Application
Open PowerShell in the project folder and run:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
mvn spring-boot:run
```

### 3. Open in Browser
- **Sign In**: [http://localhost:8081/login](http://localhost:8081/login)
- **Gym Dashboard**: [http://localhost:8081](http://localhost:8081)
- **Class Schedules**: [http://localhost:8081/classes](http://localhost:8081/classes)
- **H2 Database Console**: [http://localhost:8081/h2-console](http://localhost:8081/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:gymdb`
  - **User Name**: `sa`
  - **Password**: *(leave blank)*
- **REST API Stats**: [http://localhost:8081/api/stats](http://localhost:8081/api/stats)

---

## 🌐 Application URL Map

| Section | Route | Allowed Roles | Description |
| :--- | :--- | :--- | :--- |
| **Login** | `/login` | Public | Sign-in page with quick demo autofill buttons |
| **Role Landing** | `/default` | Authenticated | Redirects to appropriate portal by role |
| **Admin Dashboard** | `/dashboard` | `ROLE_ADMIN` | Management overview, metrics, quick check-in |
| **Coach Portal** | `/trainer/dashboard` | `ROLE_TRAINER`, `ROLE_ADMIN` | Instructor dashboard with assigned classes & athletes |
| **Member Portal** | `/member/portal` | `ROLE_MEMBER`, `ROLE_ADMIN` | Athlete hub, subscription days counter, my bookings |
| **Class Schedule** | `/classes` | Authenticated | Browse classes, view capacity & book spots |
| **Schedule Class** | `/classes/new` | `ROLE_ADMIN`, `ROLE_TRAINER` | Add a new group fitness session |
| **Class Roster** | `/classes/view/{id}` | `ROLE_ADMIN`, `ROLE_TRAINER` | View attendee list and manage roster |
| **Members List** | `/members` | `ROLE_ADMIN` | Searchable directory of all gym members |
| **Trainers** | `/trainers` | `ROLE_ADMIN` | Trainer directory with specializations & member counts |
| **Attendance** | `/attendance` | `ROLE_ADMIN`, `ROLE_TRAINER` | Check-in / Check-out desk and daily occupancy |
