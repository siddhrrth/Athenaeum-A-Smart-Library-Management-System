# Library Management System — Database Design

This directory contains the relational database definition scripts for **PostgreSQL**, designed strictly according to normalization principles (1NF → 2NF → 3NF → BCNF).

---

## 🗄️ Relational Schema (11 Tables)

| Relation | Primary Key | Foreign Keys | Description |
| :--- | :--- | :--- | :--- |
| **`PUBLISHER`** | `publisher_id` | &mdash; | Book publishers and contact details |
| **`CATEGORY`** | `category_id` | &mdash; | Academic subject classifications |
| **`AUTHOR`** | `author_id` | &mdash; | Author biographical records |
| **`BOOK`** | `book_id` | `publisher_id`, `category_id` | Catalog title, edition, total and available copy counts |
| **`BOOK_AUTHOR`** | `(book_id, author_id)` | `book_id`, `author_id` | Composite bridge resolving Many-to-Many ($M:N$) authoring |
| **`MEMBER`** | `member_id` | &mdash; | Student and faculty patron accounts |
| **`LIBRARIAN`** | `librarian_id` | &mdash; | Staff and administrative accounts (`role IN ('librarian', 'admin')`) |
| **`BORROW`** | `borrow_id` | `book_id`, `member_id`, `librarian_id` | Circulation transaction tracking issue, due, and return dates |
| **`RESERVATION`** | `reservation_id` | `book_id`, `member_id` | Waitlist holds placed when copies reach 0 |
| **`FINE`** | `fine_id` | `borrow_id UNIQUE` | Overdue penalty tracking ($1:0..1$ relationship) |
| **`FEEDBACK`** | `feedback_id` | `member_id`, `book_id NULLABLE` | Star ratings (1–5) and member reviews |

---

## 📐 Normalization Pipeline
1. **1NF (First Normal Form):** All attribute values are atomic; no multivalued attributes or repeating groups.
2. **2NF (Second Normal Form):** Every non-prime attribute is fully functionally dependent on the entire candidate key (removed partial dependencies on composite keys via `BOOK_AUTHOR`).
3. **3NF (Third Normal Form):** Every non-prime attribute is non-transitively dependent on candidate keys (separated `PUBLISHER`, `CATEGORY`, `AUTHOR`, `MEMBER`, and `LIBRARIAN`).
4. **BCNF (Boyce-Codd Normal Form):** Every determinant in all 11 relations is a superkey.

---

## 🚀 Execution & Setup

Execute the scripts using `psql`:

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE library_db;
\c library_db

# Run schema and seed scripts
\i 'path/to/database/schema.sql'
\i 'path/to/database/seed.sql'
```
