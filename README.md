#  Athenaeum — Library Management System

[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Apache Tomcat](https://img.shields.io/badge/Apache%20Tomcat-9%2B-yellow.svg?logo=apachetomcat&logoColor=white)](https://tomcat.apache.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple.svg?logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An enterprise-grade, relational-database-driven **Library Management System (LMS)** designed to digitize, streamline, and centralize academic library operations. Backed by a rigorously normalized PostgreSQL database (1NF &rarr; 2NF &rarr; 3NF &rarr; BCNF), a Java Servlets & JDBC backend deployed on Apache Tomcat, and a sleek, modern responsive web dashboard.

---

##  Overview

Manual library management and spreadsheet-based workflows suffer from lost books, inventory discrepancies, manual calculation errors for overdue penalties, and concurrent update conflicts. **Athenaeum** resolves these challenges by providing:

1. **Centralized Relational Storage:** All library data is modeled across 11 normalized tables ensuring complete data consistency and zero anomalies.
2. **Transactional Circulation:** Book checkouts and returns use atomic database transactions with automatic stock updates and penalty fee generation.
3. **Role-Based Workflows:** Distinct operational interfaces for Administrators, Librarians, and Members.
4. **Offline-Ready Web Client:** Self-hosted vendor assets guarantee complete functionality without external CDN reliance.

---

##  Repository Architecture

```
├── backend/                  # Java Servlets & JDBC Web API (Apache Tomcat)
│   ├── src/                  # Servlet controllers, CORS filter, DBConnection utility
│   ├── WEB-INF/              # Deployment descriptor (web.xml), bundled JAR dependencies
│   └── README.md             # Backend setup & deployment guide
│
├── database/                 # PostgreSQL Relational Database
│   ├── schema.sql            # Complete DDL with primary/foreign keys and CHECK constraints
│   ├── seed.sql              # Realistic seed data for catalog, patrons, and transactions
│   └── README.md             # Database architecture & normalization explanation
│
├── frontend/                 # Athenaeum Responsive Web Client
│   ├── assets/               # Self-hosted Inter font, Bootstrap 5, Bootstrap Icons, Chart.js, Favicon
│   ├── css/                  # Custom design tokens, dark mode palette, dashboard layout styles
│   ├── js/                   # Client-side controllers and API service layer
│   ├── *.html                # Complete views (Dashboard, Books, Members, Issues, Returns, etc.)
│   └── README.md             # Frontend overview & design tokens
│
├── cli-app/                  # Standalone Java Console / CLI Application
│   ├── src/                  # Pure OOP models (Book, Student, Library, BorrowRecord, FileHandler)
│   ├── output/               # Terminal execution demo screenshots
│   └── README.md             # CLI compilation & usage instructions
│
├── .gitignore                # Git exclusions (build artifacts, IDE files, OS caches)
└── README.md                 # Project documentation
```

---

## Relational Schema & Normalization

The database architecture is normalized up to **Boyce-Codd Normal Form (BCNF)** to eliminate redundancy and prevent insertion, update, and deletion anomalies.

```
PUBLISHER (1) ──< BOOK (N) >── BOOK_AUTHOR (M:N) ──< AUTHOR (M)
                     │
CATEGORY (1) ────────┘
                     │
                 BORROW (N) ──[1:0..1]── FINE
                  │      │
MEMBER (1) ───────┘      └── LIBRARIAN (1)
```

### Database Relations (11 Tables):
- **`PUBLISHER`**: Publishing house metadata and contact channels.
- **`CATEGORY`**: Academic classification taxonomy.
- **`AUTHOR`**: Author biographical records.
- **`BOOK`**: Catalog volume metadata (`isbn UNIQUE`), tracking `total_copies` and `available_copies`.
- **`BOOK_AUTHOR`**: Associative bridge entity with composite primary key `(book_id, author_id)`.
- **`MEMBER`**: Patron directory (`email UNIQUE`, `member_type IN ('student', 'faculty')`).
- **`LIBRARIAN`**: Staff registry with role discriminator (`role IN ('librarian', 'admin')`).
- **`BORROW`**: Circulation ledger tracking issues, due dates, and return timestamps.
- **`RESERVATION`**: Queue management for unavailable titles (`available_copies = 0`).
- **`FINE`**: Overdue penalty assessment linked 1:1 with transactions (`borrow_id UNIQUE`).
- **`FEEDBACK`**: Member reviews and 1–5 star ratings for books or library facilities.

---

##  Getting Started

### 1. Database Initialization (PostgreSQL)

Ensure PostgreSQL is running on `localhost:5432`:

```bash
# Connect to PostgreSQL shell
psql -U postgres

# Create and populate database
CREATE DATABASE library_db;
\c library_db
\i 'database/schema.sql'
\i 'database/seed.sql'
```

### 2. Deploy Backend (Apache Tomcat)

1. Ensure the Java classes are compiled:
   ```bash
   cd backend
   javac -cp "WEB-INF/lib/*" -d WEB-INF/classes src/com/library/**/*.java
   ```
2. Copy the `backend/` folder into your Tomcat `webapps/` directory.
3. Start Tomcat:
   ```bash
   # On Windows:
   bin/startup.bat

   # On Linux / macOS:
   bin/startup.sh
   ```

### 3. Launch Web Client

Open your browser and navigate to:
```
http://localhost:8081/backend/login.html
```

#### Demo Credentials:
| Account Type | Username / Identifier | Password | Permitted Operations |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` (or `admin@library.com`) | `admin123` | Full administrative privileges, settings & reports |
| **Librarian** | `librarian1` (or `staff@library.com`) | `staff123` | Catalog curation, circulation, and fine collection |
| **Student** | `alice@example.com` | *(ID Check)* | Catalog search, hold reservations, and loan tracking |

---

##  Standalone Console Application

In addition to the web client, a standalone Java console application is provided under `cli-app/`:

```bash
cd cli-app/src
javac *.java
java Main
```

---
