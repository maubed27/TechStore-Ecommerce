package com.techstore.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.techstore.model.CartItem;
import com.techstore.model.Order;
import com.techstore.model.OrderItem; // NEW Import
import com.techstore.util.DatabaseConnection;

/**
 * OrderDAO handles database operations for Order and OrderItem objects.
 */
public class OrderDAO {
    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    /**
     * Saves a new order and its items to the database.
     * @param order The order object to save.
     * @param cartItems The list of cart items associated with this order.
     * @return The saved order with its generated ID, or null if saving failed.
     */
    public Order saveOrder(Order order, List<CartItem> cartItems) {
        // Updated SQL to include delivery_address
        String orderSql = "INSERT INTO orders (user_id, total, status, delivery_address, created_at) VALUES (?, ?, ?, ?, ?)";
        // Updated SQL to include product_image
        String orderItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price, product_name, product_image) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            LOGGER.info("OrderDAO saveOrder: Attempting to get database connection.");
            conn = DatabaseConnection.getConnection();
            LOGGER.info("OrderDAO saveOrder: Connection obtained. AutoCommit: " + conn.getAutoCommit());
            conn.setAutoCommit(false); // Start transaction
            LOGGER.info("OrderDAO saveOrder: AutoCommit set to false.");

            // 1. Insert the order
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                LOGGER.info("OrderDAO saveOrder: Executing order insert SQL: " + orderSql);
                if (order.getUserId() != 0) {
                    pstmt.setInt(1, order.getUserId());
                    LOGGER.info("OrderDAO saveOrder: Setting user_id: " + order.getUserId());
                } else {
                    pstmt.setNull(1, Types.INTEGER);
                    LOGGER.info("OrderDAO saveOrder: Setting user_id to NULL (guest).");
                }
                pstmt.setBigDecimal(2, order.getTotal());
                pstmt.setString(3, order.getStatus());
                pstmt.setString(4, order.getDeliveryAddress()); // NEW: Set delivery address
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                int affectedRows = pstmt.executeUpdate();
                LOGGER.info("OrderDAO saveOrder: Order insert affected rows: " + affectedRows);
                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                        order.setCreatedAt(LocalDateTime.now());
                        LOGGER.info("OrderDAO saveOrder: Generated Order ID: " + order.getId());
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
            }

            // 2. Insert order items
            try (PreparedStatement pstmt = conn.prepareStatement(orderItemSql)) {
                LOGGER.info("OrderDAO saveOrder: Executing order item insert SQL: " + orderItemSql + " for " + cartItems.size() + " items.");
                for (CartItem item : cartItems) {
                    pstmt.setInt(1, order.getId());
                    if (item.getProduct() != null) {
                        pstmt.setInt(2, item.getProduct().getId());
                    } else {
                        pstmt.setNull(2, Types.INTEGER);
                    }
                    pstmt.setInt(3, item.getQuantity());
                    BigDecimal unitPrice = item.getPrice();
                    pstmt.setBigDecimal(4, unitPrice);
                    pstmt.setString(5, item.getProduct() != null ? item.getProduct().getName() : "Unknown Product");
                    pstmt.setString(6, item.getProduct() != null ? item.getProduct().getImage() : "https://placehold.co/200x200?text=No+Image"); // NEW: Save image
                    pstmt.addBatch();
                    LOGGER.info("OrderDAO saveOrder: Added item to batch: Product ID " + (item.getProduct() != null ? item.getProduct().getId() : "null") + ", Quantity " + item.getQuantity());
                }
                pstmt.executeBatch();
                LOGGER.info("OrderDAO saveOrder: Order items batch executed.");
            }

            conn.commit(); // Commit transaction
            LOGGER.info("OrderDAO saveOrder: Transaction committed successfully.");
            return order;

        } catch (SQLException e) {
            LOGGER.severe("OrderDAO saveOrder: SQL Exception during order save: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.warning("OrderDAO saveOrder: Transaction rolled back due to error.");
                } catch (SQLException ex) {
                    LOGGER.severe("OrderDAO saveOrder: Error during rollback: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                    LOGGER.info("OrderDAO saveOrder: Database connection closed.");
                } catch (SQLException e) {
                    LOGGER.severe("OrderDAO saveOrder: Error closing connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Retrieves orders for a specific user, including their items.
     * @param userId The ID of the user.
     * @return A list of orders for the user, with populated order items.
     */
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String orderSql = "SELECT id, user_id, total, status, delivery_address, created_at FROM orders WHERE user_id = ? ORDER BY created_at DESC"; // Added delivery_address
        String orderItemSql = "SELECT id, order_id, product_id, quantity, price, product_name, product_image FROM order_items WHERE order_id = ?"; // Added product_image

        try (Connection conn = DatabaseConnection.getConnection()) {
            LOGGER.info("OrderDAO getOrdersByUserId: Executing SQL: " + orderSql + " for user ID: " + userId);
            // Fetch orders
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Order order = mapResultSetToOrder(rs);
                        orders.add(order);
                    }
                }
            }

            // Fetch items for each order
            try (PreparedStatement itemPstmt = conn.prepareStatement(orderItemSql)) {
                for (Order order : orders) {
                    itemPstmt.setInt(1, order.getId());
                    try (ResultSet itemRs = itemPstmt.executeQuery()) {
                        List<OrderItem> orderItems = new ArrayList<>();
                        while (itemRs.next()) {
                            orderItems.add(mapResultSetToOrderItem(itemRs));
                        }
                        order.setOrderItems(orderItems); // Set the fetched items into the Order object
                    }
                }
            }
            LOGGER.info("OrderDAO getOrdersByUserId: Retrieved " + orders.size() + " orders for user ID: " + userId + " with items.");
        } catch (SQLException e) {
            LOGGER.severe("OrderDAO getOrdersByUserId: SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Searches orders by Order ID or product name within a user's orders.
     * @param userId The ID of the user whose orders to search.
     * @param searchTerm The term to search for (Order ID or product name).
     * @return A list of matching orders.
     */
    public List<Order> searchOrders(int userId, String searchTerm) {
        List<Order> orders = new ArrayList<>();
        // Note: This search queries orders and their items, so it's a bit more complex.
        // For simplicity, we'll fetch all orders for the user first, then filter/search.
        // A more optimized solution for large datasets would involve JOINs in SQL.
        List<Order> allUserOrders = getOrdersByUserId(userId); // Re-use existing method

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allUserOrders; // Return all if no search term
        }

        String lowerCaseSearchTerm = searchTerm.toLowerCase();
        for (Order order : allUserOrders) {
            boolean matches = false;
            // Search by Order ID
            if (String.valueOf(order.getId()).contains(lowerCaseSearchTerm)) {
                matches = true;
            }
            // Search by product name within order items
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProductName() != null && item.getProductName().toLowerCase().contains(lowerCaseSearchTerm)) {
                        matches = true;
                        break;
                    }
                }
            }
            if (matches) {
                orders.add(order);
            }
        }
        return orders;
    }


    /**
     * Helper method to map a ResultSet row to an Order object.
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setTotal(rs.getBigDecimal("total"));
        order.setStatus(rs.getString("status"));
        order.setDeliveryAddress(rs.getString("delivery_address")); // NEW: Get delivery address
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return order;
    }

    /**
     * Helper method to map a ResultSet row to an OrderItem object.
     */
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setProductImage(rs.getString("product_image")); // NEW: Get product image
        return item;
    }
}
