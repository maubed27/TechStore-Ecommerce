package com.techstore.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.techstore.model.Product;
import com.techstore.util.DatabaseConnection;

/**
 * ProductDAO handles database operations for Product objects.
 */
public class ProductDAO {
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    /**
     * Retrieves all products from the database.
     * @return A list of all products.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, description, image, stock, category, created_at FROM products"; // Added category
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Exception in getAllProducts: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves a product by its ID.
     * @param id The ID of the product.
     * @return An Optional containing the product if found, empty otherwise.
     */
    public Optional<Product> getProductById(int id) {
        String sql = "SELECT id, name, price, description, image, stock, category, created_at FROM products WHERE id = ?"; // Added category
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Adds a new product to the database.
     * @param product The product object to add.
     * @return The added product with its generated ID, or null if insertion failed.
     */
    public Product addProduct(Product product) {
        String sql = "INSERT INTO products (name, price, description, image, stock, category, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)"; // Added category
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setBigDecimal(2, product.getPrice());
            pstmt.setString(3, product.getDescription());
            pstmt.setString(4, product.getImage());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getCategory()); // Set category
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                        product.setCreatedAt(LocalDateTime.now());
                        return product;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates an existing product in the database.
     * @param product The product object with updated details.
     * @return true if the product was updated, false otherwise.
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, description = ?, image = ?, stock = ?, category = ? WHERE id = ?"; // Added category
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setBigDecimal(2, product.getPrice());
            pstmt.setString(3, product.getDescription());
            pstmt.setString(4, product.getImage());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getCategory()); // Set category
            pstmt.setInt(7, product.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a product from the database by ID.
     * @param id The ID of the product to delete.
     * @return true if the product was deleted, false otherwise.
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reduces the stock of a product by a given quantity.
     * @param productId The ID of the product.
     * @param quantity The amount to reduce stock by.
     * @return true if stock was successfully reduced, false otherwise (e.g., insufficient stock).
     */
    public boolean reduceStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity); // Ensure stock is sufficient before reducing

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a product is available in the requested quantity.
     * @param productId The ID of the product.
     * @param quantity The requested quantity.
     * @return true if available, false otherwise.
     */
    public boolean isAvailable(int productId, int quantity) {
        String sql = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    return currentStock >= quantity;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Searches for products by name.
     * @param searchTerm The term to search for in product names.
     * @return A list of matching products.
     */
    public List<Product> searchProducts(String searchTerm) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, description, image, stock, category, created_at FROM products WHERE name LIKE ?"; // Added category
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves products within a specified price range.
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @return A list of products within the price range.
     */
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, description, image, stock, category, created_at FROM products WHERE price BETWEEN ? AND ?"; // Added category
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, minPrice);
            pstmt.setBigDecimal(2, maxPrice);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Helper method to map a ResultSet row to a Product object.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setDescription(rs.getString("description"));
        product.setImage(rs.getString("image"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category")); // NEW: Get category
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            product.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }
        return product;
    }
}
