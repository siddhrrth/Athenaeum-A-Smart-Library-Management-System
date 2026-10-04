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

@WebServlet("/api/reservations/*")
public class ReservationServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                String sql = "SELECT r.*, b.title as book_title, m.name as member_name " +
                             "FROM RESERVATION r " +
                             "JOIN BOOK b ON r.book_id = b.book_id " +
                             "JOIN MEMBER m ON r.member_id = m.member_id " +
                             "ORDER BY r.reservation_date DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
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
            if (pathInfo == null || pathInfo.equals("/")) {
                String body = new String(request.getInputStream().readAllBytes());
                JsonObject json = gson.fromJson(body, JsonObject.class);
                int bookId = json.get("book_id").getAsInt();
                int memberId = json.get("member_id").getAsInt();
                
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement chk = conn.prepareStatement("SELECT available_copies FROM BOOK WHERE book_id = ? FOR UPDATE")) {
                        chk.setInt(1, bookId);
                        try (ResultSet rs = chk.executeQuery()) {
                            if (rs.next()) {
                                if (rs.getInt(1) == 0) {
                                    String insert = "INSERT INTO RESERVATION (book_id, member_id, reservation_date, expiry_date, status) VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + INTERVAL '7 days', 'pending')";
                                    try (PreparedStatement ins = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                                        ins.setInt(1, bookId);
                                        ins.setInt(2, memberId);
                                        ins.executeUpdate();
                                        conn.commit();
                                        
                                        JsonObject res = new JsonObject();
                                        res.addProperty("success", true);
                                        try (ResultSet gen = ins.getGeneratedKeys()) {
                                            if (gen.next()) res.addProperty("reservation_id", gen.getInt(1));
                                        }
                                        response.getWriter().write(gson.toJson(res));
                                    }
                                } else {
                                    throw new Exception("Book is currently available, reservation not needed");
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
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            if (pathInfo != null && pathInfo.endsWith("/cancel")) {
                String idStr = pathInfo.substring(1, pathInfo.length() - 7);
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE RESERVATION SET status = 'cancelled' WHERE reservation_id = ?")) {
                    stmt.setInt(1, Integer.parseInt(idStr));
                    int updated = stmt.executeUpdate();
                    if (updated > 0) {
                        JsonObject res = new JsonObject();
                        res.addProperty("success", true);
                        response.getWriter().write(gson.toJson(res));
                    } else {
                        throw new Exception("Reservation not found");
                    }
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
