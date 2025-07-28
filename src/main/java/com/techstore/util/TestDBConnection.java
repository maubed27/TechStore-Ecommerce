package com.techstore.util; // Ensure this package declaration is correct

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {
    public static void main(String[] args) {
        System.out.println("Attempting to connect to the database...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("SUCCESS: Connected to the database!");
            } else {
                System.out.println("FAILURE: Connection returned null.");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Could not connect to the database.");
            e.printStackTrace(); // Print the full stack trace for debugging
        }
    }
}