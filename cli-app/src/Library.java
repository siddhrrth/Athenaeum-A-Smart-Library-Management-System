import java.util.*;
import java.time.LocalDate;

public class Library {

    private static final String BOOKS_FILE = "books.csv";
    private static final String BORROW_FILE = "borrow_records.csv";

    // VIEW ALL BOOKS

    public void viewBooks() {
        List<String[]> books = FileHandler.readCSV(BOOKS_FILE);

        System.out.println("\n--- Library Books ---");
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }

        for (String[] b : books) {
            System.out.println( "ID: " + b[0] + " | Title: " + b[1] + " | Author: " + b[2] + " | Available: " + b[3]);
        }
    }

    // ADD NEW BOOK

    public void addBook(Book book) {
        FileHandler.appendCSV(BOOKS_FILE, book.toString());
        System.out.println("Book added successfully.");
    }

    // ISSUE BOOK

    public void issueBook(String studentId, String bookId) {
        List<String[]> books = FileHandler.readCSV(BOOKS_FILE);
        List<String> updatedBooks = new ArrayList<>();

        boolean issued = false;

        for (String[] b : books) {
            if (b[0].equals(bookId) && b[3].equals("true")) {
                b[3] = "false";
                issued = true;

                LocalDate issueDate = LocalDate.now();
                LocalDate dueDate = issueDate.plusDays(14);

                BorrowRecord record = new BorrowRecord(studentId,bookId,issueDate.toString(),dueDate.toString());

                FileHandler.appendCSV(BORROW_FILE, record.toString());
            }
            updatedBooks.add(String.join(",", b));
        }

        FileHandler.writeCSV(BOOKS_FILE, updatedBooks);

        if (issued) {
            System.out.println("Book issued successfully.");
        } else {
            System.out.println("Book not available or invalid ID.");
        }
    }

    // RETURN BOOK
    public void returnBook(String bookId) {
        List<String[]> books = FileHandler.readCSV(BOOKS_FILE);
        List<String> updatedBooks = new ArrayList<>();
        boolean found = false;

        for (String[] b : books) {
            if (b[0].equals(bookId)) {
                b[3] = "true";
                found = true;
            }
            updatedBooks.add(String.join(",", b));
        }

        FileHandler.writeCSV(BOOKS_FILE, updatedBooks);

        if (found) {
            System.out.println("Book returned successfully.");
        } else {
            System.out.println("Invalid Book ID.");
        }
    }

    // STUDENT VIEW ISSUED BOOKS
    public void viewIssuedBooksByStudent(String studentId) {
        List<String[]> records = FileHandler.readCSV(BORROW_FILE);

        System.out.println("\n--- Issued Books for Student ID: " + studentId + " ---");
        boolean found = false;

        for (String[] r : records) {
            if (r[0].equals(studentId)) {
                System.out.println( "Book ID: " + r[1] + " | Issue Date: " + r[2] + " | Due Date: " + r[3]);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No books issued");
        }
    }
}
