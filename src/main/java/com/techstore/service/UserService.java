package com.techstore.service;

import java.util.Optional;

import com.techstore.dao.UserDAO; // Import the new UserDAO
import com.techstore.model.User;

/**
 * UserService handles business logic for User operations.
 * Interacts with UserDAO for database persistence.
 */
public class UserService {
    private static UserService instance; // Singleton pattern
    private UserDAO userDAO;

    private UserService() {
        this.userDAO = new UserDAO(); // Initialize the DAO
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Registers a new user.
     * @param user The user object to register.
     * @return An Optional containing the registered user with ID, or Optional.empty() if registration failed (e.g., email exists).
     */
    public Optional<User> registerUser(User user) {
        // Check if user with this email already exists
        if (userDAO.findUserByEmail(user.getEmail()).isPresent()) {
            return Optional.empty(); // User already exists
        }
        // In a real application, hash the password before saving
        User savedUser = userDAO.saveUser(user);
        return Optional.ofNullable(savedUser);
    }

    /**
     * Authenticates a user.
     * @param email The user's email.
     * @param password The user's password.
     * @return An Optional containing the user if authenticated, empty otherwise.
     */
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userDAO.findUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // In a real application, compare hashed passwords
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
