DROP TABLE IF EXISTS FEEDBACK;
DROP TABLE IF EXISTS FINE;
DROP TABLE IF EXISTS RESERVATION;
DROP TABLE IF EXISTS BORROW;
DROP TABLE IF EXISTS LIBRARIAN;
DROP TABLE IF EXISTS MEMBER;
DROP TABLE IF EXISTS BOOK_AUTHOR;
DROP TABLE IF EXISTS BOOK;
DROP TABLE IF EXISTS AUTHOR;
DROP TABLE IF EXISTS CATEGORY;
DROP TABLE IF EXISTS PUBLISHER;

-- PUBLISHER table
CREATE TABLE PUBLISHER(
    publisher_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    address VARCHAR(255),
    phone VARCHAR(15),
    email VARCHAR(100)
);

-- CATEGORY table
CREATE TABLE CATEGORY(
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100),
    description VARCHAR(255)
);

-- AUTHOR table
CREATE TABLE AUTHOR(
    author_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    nationality VARCHAR(50)
);

-- BOOK table
CREATE TABLE BOOK(
    book_id SERIAL PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(200),
    edition VARCHAR(50),
    publication_year INT,
    total_copies INT,
    available_copies INT CHECK (available_copies >= 0 AND available_copies <= total_copies),
    publisher_id INT,
    category_id INT,
    FOREIGN KEY (publisher_id) REFERENCES PUBLISHER(publisher_id),
    FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id)
);

-- BOOK_AUTHOR table
CREATE TABLE BOOK_AUTHOR(
    book_id INT,
    author_id INT,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES BOOK(book_id),
    FOREIGN KEY (author_id) REFERENCES AUTHOR(author_id)
);

-- MEMBER table
CREATE TABLE MEMBER(
    member_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    address VARCHAR(255),
    member_type VARCHAR(20) CHECK (member_type IN ('student', 'faculty')),
    registration_date DATE,
    status VARCHAR(20) CHECK (status IN ('active', 'inactive'))
);

-- LIBRARIAN table
CREATE TABLE LIBRARIAN(
    librarian_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(15),
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(20) CHECK (role IN ('librarian', 'admin')),
    status VARCHAR(20) CHECK (status IN ('active', 'inactive'))
);

-- BORROW table
CREATE TABLE BORROW(
    borrow_id SERIAL PRIMARY KEY,
    book_id INT,
    member_id INT,
    librarian_id INT,
    issue_date DATE,
    due_date DATE,
    return_date DATE,
    status VARCHAR(20) CHECK (status IN ('issued', 'returned')),
    FOREIGN KEY (book_id) REFERENCES BOOK(book_id),
    FOREIGN KEY (member_id) REFERENCES MEMBER(member_id),
    FOREIGN KEY (librarian_id) REFERENCES LIBRARIAN(librarian_id)
);

-- RESERVATION table
CREATE TABLE RESERVATION(
    reservation_id SERIAL PRIMARY KEY,
    book_id INT,
    member_id INT,
    reservation_date DATE,
    expiry_date DATE,
    status VARCHAR(20) CHECK (status IN ('pending', 'fulfilled', 'cancelled', 'expired')),
    FOREIGN KEY (book_id) REFERENCES BOOK(book_id),
    FOREIGN KEY (member_id) REFERENCES MEMBER(member_id)
);

-- FINE table
CREATE TABLE FINE(
    fine_id SERIAL PRIMARY KEY,
    borrow_id INT UNIQUE,
    amount DECIMAL(10,2),
    fine_date DATE,
    paid_status VARCHAR(20) CHECK (paid_status IN ('paid', 'unpaid')),
    paid_date DATE,
    FOREIGN KEY (borrow_id) REFERENCES BORROW(borrow_id)
);

-- FEEDBACK table
CREATE TABLE FEEDBACK(
    feedback_id SERIAL PRIMARY KEY,
    member_id INT,
    book_id INT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comments VARCHAR(1000),
    feedback_date DATE,
    FOREIGN KEY (member_id) REFERENCES MEMBER(member_id),
    FOREIGN KEY (book_id) REFERENCES BOOK(book_id)
);
