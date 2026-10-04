-- 3 Publishers
INSERT INTO PUBLISHER (name, address, phone, email) VALUES
('Penguin Random House', '1745 Broadway, New York, NY', '123-456-7890', 'contact@penguin.com'),
('HarperCollins', '195 Broadway, New York, NY', '234-567-8901', 'info@harpercollins.com'),
('O''Reilly Media', '1005 Gravenstein Hwy North, Sebastopol, CA', '345-678-9012', 'support@oreilly.com');

-- 5 Categories
INSERT INTO CATEGORY (category_name, description) VALUES
('Fiction', 'Fictional stories and novels'),
('Science', 'Scientific concepts and discoveries'),
('Technology', 'Computer science, engineering and technology'),
('History', 'Historical events and analyses'),
('Philosophy', 'Study of general and fundamental questions');

-- 6 Authors
INSERT INTO AUTHOR (name, nationality) VALUES
('J.K. Rowling', 'British'),
('Isaac Asimov', 'American'),
('Martin Kleppmann', 'German'),
('Yuval Noah Harari', 'Israeli'),
('Friedrich Nietzsche', 'German'),
('Arthur C. Clarke', 'British');

-- 10 Books
-- 2 copies of book 1, 2 copies of book 3, 1 copy of book 4, and 1 copy of book 8 are currently issued
INSERT INTO BOOK (isbn, title, edition, publication_year, total_copies, available_copies, publisher_id, category_id) VALUES
('978-0439708180', 'Harry Potter and the Sorcerer''s Stone', '1st Edition', 1997, 10, 8, 1, 1),
('978-0553293357', 'Foundation', 'Reprint Edition', 1951, 5, 5, 1, 2),
('978-1491903063', 'Designing Data-Intensive Applications', '1st Edition', 2017, 12, 10, 3, 3),
('978-0062316097', 'Sapiens: A Brief History of Humankind', '1st Edition', 2015, 8, 7, 2, 4),
('978-0140449235', 'Beyond Good and Evil', 'Revised Edition', 2003, 4, 4, 1, 5),
('978-0451457998', '2001: A Space Odyssey', '1st Edition', 1968, 6, 6, 1, 2),
('978-0439064873', 'Harry Potter and the Chamber of Secrets', '1st Edition', 1998, 10, 10, 1, 1),
('978-0062842183', '21 Lessons for the 21st Century', '1st Edition', 2018, 7, 6, 2, 4),
('978-1449373320', 'Zero to One', '1st Edition', 2014, 5, 5, 3, 3),
('978-0553283686', 'I, Robot', 'Mass Market Paperback', 1991, 8, 8, 1, 2);

-- Book-Author Associations
INSERT INTO BOOK_AUTHOR (book_id, author_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 1),
(8, 4),
(9, 3),
(10, 2);

-- 5 Members
INSERT INTO MEMBER (name, email, phone, address, member_type, registration_date, status) VALUES
('Alice Smith', 'alice@example.com', '555-0101', '123 Main St, Anytown', 'student', '2023-01-15', 'active'),
('Bob Johnson', 'bob@example.com', '555-0102', '456 Elm St, Anytown', 'student', '2023-02-20', 'active'),
('Carol Williams', 'carol@example.com', '555-0103', '789 Oak St, Anytown', 'faculty', '2022-08-10', 'active'),
('David Brown', 'david@example.com', '555-0104', '321 Pine St, Anytown', 'student', '2023-03-05', 'inactive'),
('Eve Davis', 'eve@example.com', '555-0105', '654 Maple St, Anytown', 'faculty', '2021-11-22', 'active');

-- 2 Librarians
INSERT INTO LIBRARIAN (name, email, phone, username, password_hash, role, status) VALUES
('Admin User', 'admin@library.com', '555-1000', 'admin', 'admin123', 'admin', 'active'),
('Lib Staff', 'staff@library.com', '555-2000', 'librarian1', 'staff123', 'librarian', 'active');

-- 8 Borrow Records (6 currently issued, 2 returned)
-- To match available_copies:
-- Book 1: 2 issued
-- Book 3: 2 issued
-- Book 4: 1 issued
-- Book 8: 1 issued
INSERT INTO BORROW (book_id, member_id, librarian_id, issue_date, due_date, return_date, status) VALUES
(1, 1, 2, '2023-09-01', '2023-09-15', '2023-09-20', 'returned'),
(2, 2, 2, '2023-09-05', '2023-09-19', '2023-09-18', 'returned'),
(1, 4, 2, '2023-10-01', '2023-10-15', NULL, 'issued'),
(1, 5, 2, '2023-10-02', '2023-10-16', NULL, 'issued'),
(3, 3, 2, '2023-10-02', '2023-10-16', NULL, 'issued'),
(3, 1, 2, '2023-10-03', '2023-10-17', NULL, 'issued'),
(4, 2, 2, '2023-10-04', '2023-10-18', NULL, 'issued'),
(8, 3, 2, '2023-10-05', '2023-10-19', NULL, 'issued');

-- 2 Fines
INSERT INTO FINE (borrow_id, amount, fine_date, paid_status, paid_date) VALUES
(1, 5.00, '2023-09-15', 'paid', '2023-09-16'),
(3, 10.00, '2023-10-16', 'unpaid', NULL);

-- 3 Reservations
INSERT INTO RESERVATION (book_id, member_id, reservation_date, expiry_date, status) VALUES
(1, 3, '2023-10-05', '2023-10-12', 'pending'),
(3, 1, '2023-10-06', '2023-10-13', 'pending'),
(4, 2, '2023-09-01', '2023-09-08', 'expired');

-- 4 Feedback
INSERT INTO FEEDBACK (member_id, book_id, rating, comments, feedback_date) VALUES
(1, 1, 5, 'Great book, loved the story.', '2023-09-15'),
(2, 2, 4, 'Classic sci-fi.', '2023-09-19'),
(3, 3, 5, 'Very informative.', '2023-09-22'),
(4, NULL, 4, 'The library is well-organized, but needs more seating.', '2023-10-01');
