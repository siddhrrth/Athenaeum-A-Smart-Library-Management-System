package com.library.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.library.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/borrows/*")
public class BorrowServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT br.*, b.title as book_title, m.name as member_name " +
                         "FROM BORROW br " +
                         "JOIN BOOK b ON br.book_id = b.book_id " +
                         "JOIN MEMBER m ON br.member_id = m.member_id ";
            
            boolean hasMemberFilter = pathInfo != null && pathInfo.startsWith("/member/");
            if (hasMemberFilter) {
                sql += "WHERE br.member_id = ? ";
            }
            sql += "ORDER BY br.issue_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (hasMemberFilter) {
                    stmt.setInt(1, Integer.parseInt(pathInfo.substring(8)));
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for(int i = 1; i <= columns; ++i) {
                            row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                        }
                        list.add(row);
                    }
                    response.getWriter().write(gson.toJson(list));
                }
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            String body = new String(request.getInputStream().readAllBytes());
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            if ("/issue".equals(pathInfo)) {
                conn.setAutoCommit(false);
                try {
                    int bookId = json.get("book_id").getAsInt();
                    try (PreparedStatement chk = conn.prepareStatement("SELECT available_copies FROM BOOK WHERE book_id = ? FOR UPDATE")) {
                        chk.setInt(1, bookId);
                        try (ResultSet rs = chk.executeQuery()) {
                            if (rs.next()) {
                                int avail = rs.getInt(1);
                                if (avail > 0) {
                                    String insert = "INSERT INTO BORROW (book_id, member_id, librarian_id, issue_date, due_date, status) VALUES (?, ?, ?, CURRENT_DATE, CURRENT_DATE + INTERVAL '14 days', 'issued')";
                                    try (PreparedStatement ins = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                                        ins.setInt(1, bookId);
                                        ins.setInt(2, json.get("member_id").getAsInt());
                                        ins.setInt(3, json.get("librarian_id").getAsInt());
                                        ins.executeUpdate();
                                        
                                        try (PreparedStatement upd = conn.prepareStatement("UPDATE BOOK SET available_copies = available_copies - 1 WHERE book_id = ?")) {
                                            upd.setInt(1, bookId);
                                            upd.executeUpdate();
                                        }
                                        conn.commit();
                                        
                                        JsonObject res = new JsonObject();
                                        res.addProperty("success", true);
                                        try (ResultSet gen = ins.getGeneratedKeys()) {
                                            if (gen.next()) res.addProperty("borrow_id", gen.getInt(1));
                                        }
                                        response.getWriter().write(gson.toJson(res));
                                    }
                                } else {
                                    throw new Exception("No copies available");
                                }
                            } else {
                                throw new Exception("Book not found");
                            }
                        }
                    }
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            } else if ("/return".equals(pathInfo)) {
                conn.setAutoCommit(false);
                try {
                    int borrowId = json.get("borrow_id").getAsInt();
                    try (PreparedStatement chk = conn.prepareStatement("SELECT * FROM BORROW WHERE borrow_id = ? FOR UPDATE")) {
                        chk.setInt(1, borrowId);
                        try (ResultSet rs = chk.executeQuery()) {
                            if (rs.next()) {
                                String status = rs.getString("status");
                                if (!"returned".equals(status)) {
                                    int bookId = rs.getInt("book_id");
                                    Date dueDate = rs.getDate("due_date");
                                    
                                    try (PreparedStatement upd = conn.prepareStatement("UPDATE BORROW SET return_date = CURRENT_DATE, status = 'returned' WHERE borrow_id = ?")) {
                                        upd.setInt(1, borrowId);
                                        upd.executeUpdate();
                                    }
                                    try (PreparedStatement updB = conn.prepareStatement("UPDATE BOOK SET available_copies = available_copies + 1 WHERE book_id = ?")) {
                                        updB.setInt(1, bookId);
                                        updB.executeUpdate();
                                    }
                                    
                                    long currentMillis = System.currentTimeMillis();
                                    long dueMillis = dueDate.getTime();
                                    if (currentMillis > dueMillis) {
                                        long overdueDays = (currentMillis - dueMillis) / (1000 * 60 * 60 * 24);
                                        if (overdueDays > 0) {
                                            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO FINE (borrow_id, amount, fine_date, paid_status) VALUES (?, ?, CURRENT_DATE, 'unpaid')")) {
                                                ins.setInt(1, borrowId);
                                                ins.setDouble(2, overdueDays * 1.0);
                                                ins.executeUpdate();
                                            }
                                        }
                                    }
                                    conn.commit();
                                    JsonObject res = new JsonObject();
                                    res.addProperty("success", true);
                                    response.getWriter().write(gson.toJson(res));
                                } else {
                                    throw new Exception("Book already returned");
                                }
                            } else {
                                throw new Exception("Borrow record not found");
                            }
                        }
                    }
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    private void handleError(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        JsonObject err = new JsonObject();
        err.addProperty("success", false);
        err.addProperty("message", e.getMessage());
        response.getWriter().write(gson.toJson(err));
    }
}
