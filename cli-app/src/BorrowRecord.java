public class BorrowRecord {
    private String studentId;
    private String bookId;
    private String issueDate;
    private String dueDate;

    public BorrowRecord(String studentId, String bookId, String issueDate, String dueDate) {
        this.studentId = studentId;
        this.bookId = bookId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return studentId + "," + bookId + "," + issueDate + "," + dueDate;
    }
}