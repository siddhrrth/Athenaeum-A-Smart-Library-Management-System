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

@WebServlet("/api/authors/*")
public class AuthorServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM AUTHOR");
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
            
            String insert = "INSERT INTO AUTHOR (name, nationality) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, json.get("name").getAsString());
                stmt.setString(2, json.has("nationality") ? json.get("nationality").getAsString() : null);
                stmt.executeUpdate();
                
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                try (ResultSet gen = stmt.getGeneratedKeys()) {
                    if (gen.next()) res.addProperty("author_id", gen.getInt(1));
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
