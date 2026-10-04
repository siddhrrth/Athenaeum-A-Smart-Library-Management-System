package com.library.servlet;

import com.google.gson.Gson;
import com.library.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DashboardServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/stats")) {
            getStats(response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Endpoint not found");
            response.getWriter().write(gson.toJson(error));
        }
    }

    private void getStats(HttpServletResponse response) throws IOException {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            stats.put("total_books", getLongStat(conn, "SELECT SUM(total_copies) FROM BOOK"));
            stats.put("available_books", getLongStat(conn, "SELECT SUM(available_copies) FROM BOOK"));
            stats.put("issued_books", getLongStat(conn, "SELECT COUNT(*) FROM BORROW WHERE status = 'issued'"));
            stats.put("total_members", getLongStat(conn, "SELECT COUNT(*) FROM MEMBER"));
            stats.put("overdue_count", getLongStat(conn, "SELECT COUNT(*) FROM BORROW WHERE status = 'issued' AND due_date < CURRENT_DATE"));
            stats.put("total_fines_collected", getDoubleStat(conn, "SELECT SUM(amount) FROM FINE WHERE paid_status = 'paid'"));
            stats.put("pending_returns", getLongStat(conn, "SELECT COUNT(*) FROM BORROW WHERE status = 'issued'"));
            stats.put("active_reservations", getLongStat(conn, "SELECT COUNT(*) FROM RESERVATION WHERE status = 'pending'"));

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("stats", stats);

            response.getWriter().write(gson.toJson(successResponse));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "An error occurred while fetching stats");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    private long getLongStat(Connection conn, String query) throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private double getDoubleStat(Connection conn, String query) throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
}
