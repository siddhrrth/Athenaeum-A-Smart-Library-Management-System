package com.library.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.library.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/books/*")
public class BookServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try (Connection conn = DBConnection.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/available")) {
                String sql = "SELECT b.*, p.name as publisher_name, c.category_name, a.author_id, a.name as author_name " +
                             "FROM BOOK b " +
                             "LEFT JOIN PUBLISHER p ON b.publisher_id = p.publisher_id " +
                             "LEFT JOIN CATEGORY c ON b.category_id = c.category_id " +
                             "LEFT JOIN BOOK_AUTHOR ba ON b.book_id = ba.book_id " +
                             "LEFT JOIN AUTHOR a ON ba.author_id = a.author_id ";
                if ("/available".equals(pathInfo)) {
                    sql += "WHERE b.available_copies > 0 ";
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    Map<Integer, JsonObject> booksMap = new HashMap<>();
                    
                    while (rs.next()) {
                        int bookId = rs.getInt("book_id");
                        JsonObject book = booksMap.get(bookId);
                        if (book == null) {
                            book = new JsonObject();
                            book.addProperty("book_id", bookId);
                            book.addProperty("isbn", rs.getString("isbn"));
                            book.addProperty("title", rs.getString("title"));
                            book.addProperty("edition", rs.getString("edition"));
                            book.addProperty("publication_year", rs.getInt("publication_year"));
                            book.addProperty("total_copies", rs.getInt("total_copies"));
                            book.addProperty("available_copies", rs.getInt("available_copies"));
                            book.addProperty("publisher_id", rs.getInt("publisher_id"));
                            book.addProperty("publisher_name", rs.getString("publisher_name"));
                            book.addProperty("category_id", rs.getInt("category_id"));
                            book.addProperty("category_name", rs.getString("category_name"));
                            book.add("authors", new JsonArray());
                            booksMap.put(bookId, book);
                        }
                        
                        int authorId = rs.getInt("author_id");
                        if (!rs.wasNull()) {
                            JsonObject author = new JsonObject();
                            author.addProperty("author_id", authorId);
                            author.addProperty("name", rs.getString("author_name"));
                            book.getAsJsonArray("authors").add(author);
                        }
                    }
                    
                    JsonArray result = new JsonArray();
                    for (JsonObject b : booksMap.values()) {
                        result.add(b);
                    }
                    response.getWriter().write(gson.toJson(result));
                }
            } else {
                String idStr = pathInfo.substring(1);
                int bookId = Integer.parseInt(idStr);
                
                String sql = "SELECT b.*, p.name as publisher_name, c.category_name, a.author_id, a.name as author_name " +
                             "FROM BOOK b " +
                             "LEFT JOIN PUBLISHER p ON b.publisher_id = p.publisher_id " +
                             "LEFT JOIN CATEGORY c ON b.category_id = c.category_id " +
                             "LEFT JOIN BOOK_AUTHOR ba ON b.book_id = ba.book_id " +
                             "LEFT JOIN AUTHOR a ON ba.author_id = a.author_id " +
                             "WHERE b.book_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, bookId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        JsonObject book = null;
                        while (rs.next()) {
                            if (book == null) {
                                book = new JsonObject();
                                book.addProperty("book_id", rs.getInt("book_id"));
                                book.addProperty("isbn", rs.getString("isbn"));
                                book.addProperty("title", rs.getString("title"));
                                book.addProperty("edition", rs.getString("edition"));
                                book.addProperty("publication_year", rs.getInt("publication_year"));
                                book.addProperty("total_copies", rs.getInt("total_copies"));
                                book.addProperty("available_copies", rs.getInt("available_copies"));
                                book.addProperty("publisher_id", rs.getInt("publisher_id"));
                                book.addProperty("publisher_name", rs.getString("publisher_name"));
                                book.addProperty("category_id", rs.getInt("category_id"));
                                book.addProperty("category_name", rs.getString("category_name"));
                                book.add("authors", new JsonArray());
                            }
                            int authorId = rs.getInt("author_id");
                            if (!rs.wasNull()) {
                                JsonObject author = new JsonObject();
                                author.addProperty("author_id", authorId);
                                author.addProperty("name", rs.getString("author_name"));
                                book.getAsJsonArray("authors").add(author);
                            }
                        }
                        if (book != null) {
                            response.getWriter().write(gson.toJson(book));
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            JsonObject err = new JsonObject();
                            err.addProperty("success", false);
                            err.addProperty("message", "Book not found");
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
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            try (Connection conn = DBConnection.getConnection()) {
                String body = new String(request.getInputStream().readAllBytes());
                JsonObject json = gson.fromJson(body, JsonObject.class);
                
                conn.setAutoCommit(false);
                try {
                    String insertBook = "INSERT INTO BOOK (title, isbn, edition, publication_year, total_copies, available_copies, publisher_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, json.get("title").getAsString());
                        stmt.setString(2, json.get("isbn").getAsString());
                        stmt.setString(3, json.get("edition").getAsString());
                        stmt.setInt(4, json.get("publication_year").getAsInt());
                        stmt.setInt(5, json.get("total_copies").getAsInt());
                        stmt.setInt(6, json.get("available_copies").getAsInt());
                        stmt.setInt(7, json.get("publisher_id").getAsInt());
                        stmt.setInt(8, json.get("category_id").getAsInt());
                        stmt.executeUpdate();
                        
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int bookId = rs.getInt(1);
                                if (json.has("author_ids") && json.get("author_ids").isJsonArray()) {
                                    JsonArray authorIds = json.getAsJsonArray("author_ids");
                                    String insertAuthor = "INSERT INTO BOOK_AUTHOR (book_id, author_id) VALUES (?, ?)";
                                    try (PreparedStatement authStmt = conn.prepareStatement(insertAuthor)) {
                                        for (JsonElement el : authorIds) {
                                            authStmt.setInt(1, bookId);
                                            authStmt.setInt(2, el.getAsInt());
                                            authStmt.addBatch();
                                        }
                                        authStmt.executeBatch();
                                    }
                                }
                                conn.commit();
                                JsonObject res = new JsonObject();
                                res.addProperty("success", true);
                                res.addProperty("book_id", bookId);
                                response.getWriter().write(gson.toJson(res));
                            }
                        }
                    }
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            } catch (Exception e) {
                handleError(response, e);
            }
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
