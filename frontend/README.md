# Athenaeum — Web Frontend Client

A responsive, modern web interface for the Library Management System built with semantic HTML5, custom CSS design tokens, Bootstrap 5, Bootstrap Icons, and vanilla JavaScript.

---

## 🎨 Design System & Highlights
- **100% Offline-Ready:** All vendor assets (Inter font family, Bootstrap CSS/JS, Bootstrap Icons font, Chart.js) are self-hosted in `assets/vendor/` with zero external CDN dependencies.
- **Dark Mode Support:** Full dark theme toggle with state persisted to `localStorage`.
- **Role Synchronization:** Dynamic navigation and role indicator badges driven by active session state.
- **Visual Analytics:** Interactive Chart.js graphs for circulation history and category distributions.

---

## 📱 Page Overview

| Page | File | Description |
| :--- | :--- | :--- |
| **Authentication** | `login.html` | Role selector (Admin, Librarian, Student), input validation, demo fallback |
| **Dashboard** | `dashboard.html` | Summary metric cards, circulation trend line chart, category doughnut chart |
| **Book Catalog** | `books.html` | Catalog table with search, category filtering, and "Add Book" modal |
| **Member Directory** | `members.html` | Patron list (Student/Faculty) with status filtering and "Add Member" modal |
| **Issue Books** | `issue-book.html` | Book checkout form with live inventory checks and active loan table |
| **Return Books** | `return-book.html` | Book check-in interface with automated overdue penalty calculator |
| **Reservations** | `reservations.html` | Waitlist queue with cancellation actions for pending reservations |
| **Fine Management** | `fines.html` | Overdue fee records with online settlement actions |
| **Reports** | `reports.html` | Analytical breakdowns of catalog status and member distribution |
| **User Profile** | `profile.html` | Session information, database engine diagnostics, and logout |

---

## 🚀 How to Run
Open `login.html` in any modern web browser or serve it through Apache Tomcat at `http://localhost:8081/backend/login.html`.
