# Library Management System — CLI Application

A lightweight, standalone Java console application demonstrating Object-Oriented Programming (OOP) principles and flat-file CSV persistence for library book circulation.

---

## 📌 Features
- **Administrator Menu:**
  - View all books in catalog
  - Add new books
  - Issue books to students
  - Process book returns
- **Student Menu:**
  - Browse available books
  - Check currently issued books by Student ID
- **Persistence:**
  - Flat-file storage using `books.csv` and `borrow_records.csv`

---

## 🏗️ Architecture & Class Structure
- `Main.java`: Interactive command-line interface driver and role menu router.
- `Library.java`: Core service layer managing catalog operations and issue/return business logic.
- `Book.java`: Domain entity representing a book title, author, and availability status.
- `Student.java`: Domain entity representing a student borrower.
- `BorrowRecord.java`: Transaction model recording book ID, student ID, issue date, and 14-day due date.
- `FileHandler.java`: File I/O utility for reading and writing CSV files.

---

## 🚀 How to Run

1. Open your terminal in the `src/` directory:
   ```bash
   cd cli-app/src
   ```

2. Compile all Java source files:
   ```bash
   javac *.java
   ```

3. Run the application:
   ```bash
   java Main
   ```

---

## 📸 Screenshots
Execution screenshots demonstrating admin workflows and student queries are available in the [`output/`](output/) directory.
