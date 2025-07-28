package com.techstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // IMPORTANT: Update these credentials if your MySQL setup is different
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/techstore?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345678";

    public static Connection getConnection() throws SQLException {
        try {
            // Ensure MySQL JDBC Driver is in your classpath (from pom.xml dependency)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Make sure it's in your classpath.", e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace(); // Log this in a real application
            }
        }
    }
}
