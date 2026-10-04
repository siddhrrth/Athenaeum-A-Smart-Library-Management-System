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

@WebServlet("/api/fines/*")
public class FineServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.*, b.title as book_title, m.name as member_name, m.member_id " +
                         "FROM FINE f " +
                         "JOIN BORROW br ON f.borrow_id = br.borrow_id " +
                         "JOIN BOOK b ON br.book_id = b.book_id " +
                         "JOIN MEMBER m ON br.member_id = m.member_id ";
            
            boolean hasMemberFilter = pathInfo != null && pathInfo.startsWith("/member/");
            if (hasMemberFilter) {
                sql += "WHERE br.member_id = ? ";
            }
            sql += "ORDER BY f.fine_date DESC";
            
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            if (pathInfo != null && pathInfo.endsWith("/pay")) {
                String idStr = pathInfo.substring(1, pathInfo.length() - 4);
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE FINE SET paid_status = 'paid', paid_date = CURRENT_DATE WHERE fine_id = ? RETURNING *")) {
                    stmt.setInt(1, Integer.parseInt(idStr));
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            ResultSetMetaData md = rs.getMetaData();
                            for(int i = 1; i <= md.getColumnCount(); ++i) {
                                row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                            }
                            response.getWriter().write(gson.toJson(row));
                        } else {
                            throw new Exception("Fine not found");
                        }
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
