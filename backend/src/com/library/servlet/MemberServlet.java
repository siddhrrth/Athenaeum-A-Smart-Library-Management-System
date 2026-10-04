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

@WebServlet("/api/members/*")
public class MemberServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        try (Connection conn = DBConnection.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM MEMBER");
                     ResultSet rs = stmt.executeQuery()) {
                    List<Map<String, Object>> members = new ArrayList<>();
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for(int i = 1; i <= columns; ++i) {
                            row.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                        }
                        members.add(row);
                    }
                    response.getWriter().write(gson.toJson(members));
                }
            } else {
                String idStr = pathInfo.substring(1);
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM MEMBER WHERE member_id = ?")) {
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
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            JsonObject err = new JsonObject();
                            err.addProperty("success", false);
                            err.addProperty("message", "Member not found");
                            response.getWriter().write(gson.toJson(err));
                        }
                    }
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
            String sql = "INSERT INTO MEMBER (name, email, phone, address, member_type, registration_date, status) VALUES (?, ?, ?, ?, ?, CURRENT_DATE, 'active')";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, json.get("name").getAsString());
                stmt.setString(2, json.get("email").getAsString());
                stmt.setString(3, json.get("phone").getAsString());
                stmt.setString(4, json.get("address").getAsString());
                stmt.setString(5, json.get("member_type").getAsString());
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        JsonObject res = new JsonObject();
                        res.addProperty("success", true);
                        res.addProperty("member_id", rs.getInt(1));
                        response.getWriter().write(gson.toJson(res));
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
