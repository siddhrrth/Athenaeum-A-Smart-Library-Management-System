# Library Management System — Java Servlets Backend (Apache Tomcat)

This directory contains the Java Servlet & JDBC REST-style web backend designed to run on **Apache Tomcat 9+** and communicate with **PostgreSQL**.

---

## 🏛️ Architecture & Endpoints

All servlets reside under `src/com/library/servlet/`:

| Servlet | URL Pattern | Methods | Description |
| :--- | :--- | :--- | :--- |
| **`AuthServlet`** | `/api/auth/login` | `POST` | Role-based authentication (Admin, Librarian, Student) |
| **`DashboardServlet`** | `/api/dashboard/stats` | `GET` | Aggregated live metrics (totals, available, issued, overdue, fines) |
| **`BookServlet`** | `/api/books/*` | `GET`, `POST` | Catalog listing with authors/publishers, book details, and additions |
| **`MemberServlet`** | `/api/members/*` | `GET`, `POST` | Member directory listing and registration |
| **`BorrowServlet`** | `/api/borrows/*` | `GET`, `POST` | Transactional issue (`/issue`) and return (`/return`) with auto-fines |
| **`FineServlet`** | `/api/fines/*` | `GET`, `PUT` | Fine records listing and settlement (`/{id}/pay`) |
| **`ReservationServlet`** | `/api/reservations/*` | `GET`, `POST`, `PUT` | Reservation queue handling and cancellations |
| **`FeedbackServlet`** | `/api/feedback/*` | `GET`, `POST` | Member feedback and ratings |
| **`PublisherServlet`** | `/api/publishers/*` | `GET`, `POST` | Publisher lookup CRUD |
| **`CategoryServlet`** | `/api/categories/*` | `GET`, `POST` | Subject category lookup CRUD |
| **`AuthorServlet`** | `/api/authors/*` | `GET`, `POST` | Author lookup CRUD |

---

## ⚙️ Key Utilities & Filters
- **`CorsFilter.java`**: Implements global CORS handling to permit browser requests across origins.
- **`DBConnection.java`**: Centralized JDBC connection manager configured for PostgreSQL (`library_db`).

---

## 🚀 How to Build & Deploy

### Prerequisites
- JDK 11 or higher
- Apache Tomcat 9+
- PostgreSQL running on port 5432

### Compilation
From the `backend/` directory:
```bash
# Compile all servlets using the bundled libraries in WEB-INF/lib
javac -cp "WEB-INF/lib/*" -d WEB-INF/classes src/com/library/**/*.java
```

### Deployment
1. Copy the `backend/` folder into your Tomcat `webapps/` folder:
   ```bash
   cp -r backend /path/to/tomcat/webapps/
   ```
2. Start Tomcat (`bin/startup.bat` on Windows or `bin/startup.sh` on Linux/macOS).
3. The API will be accessible at:
   ```
   http://localhost:8081/backend/api/...
   ```
