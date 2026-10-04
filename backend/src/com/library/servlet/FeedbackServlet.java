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

@WebServlet("/api/feedback/*")
public class FeedbackServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.*, m.name as member_name, b.title as book_title " +
                         "FROM FEEDBACK f " +
                         "JOIN MEMBER m ON f.member_id = m.member_id " +
                         "LEFT JOIN BOOK b ON f.book_id = b.book_id ";
            
            boolean hasBookFilter = pathInfo != null && pathInfo.startsWith("/book/");
            if (hasBookFilter) {
                sql += "WHERE f.book_id = ? ";
            }
            sql += "ORDER BY f.feedback_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (hasBookFilter) {
                    stmt.setInt(1, Integer.parseInt(pathInfo.substring(6)));
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
        
        try (Connection conn = DBConnection.getConnection()) {
            String body = new String(request.getInputStream().readAllBytes());
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String insert = "INSERT INTO FEEDBACK (member_id, book_id, rating, comments, feedback_date) VALUES (?, ?, ?, ?, CURRENT_DATE)";
            try (PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, json.get("member_id").getAsInt());
                if (json.has("book_id") && !json.get("book_id").isJsonNull()) {
                    stmt.setInt(2, json.get("book_id").getAsInt());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setInt(3, json.get("rating").getAsInt());
                stmt.setString(4, json.get("comments").getAsString());
                stmt.executeUpdate();
                
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                try (ResultSet gen = stmt.getGeneratedKeys()) {
                    if (gen.next()) res.addProperty("feedback_id", gen.getInt(1));
                }
                response.getWriter().write(gson.toJson(res));
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
