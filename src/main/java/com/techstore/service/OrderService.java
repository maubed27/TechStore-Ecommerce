package com.techstore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.techstore.dao.OrderDAO;
import com.techstore.dao.ProductDAO;
import com.techstore.model.CartItem;
import com.techstore.model.Order;

/**
 * OrderService handles business logic for Order operations.
 * Interacts with OrderDAO and ProductDAO.
 */
public class OrderService {
    private static OrderService instance;
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    private OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
    }

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    /**
     * Processes a new order. This includes saving the order to DB and reducing product stock.
     * @param cartItems The items in the cart.
     * @param userId The ID of the logged-in user (0 for guest).
     * @param total The total amount of the order.
     * @param deliveryAddress The delivery address provided during checkout. // NEW Parameter
     * @return The created Order object, or null if processing failed.
     */
    public Order processOrder(List<CartItem> cartItems, int userId, BigDecimal total, String deliveryAddress) { // NEW Parameter
        LOGGER.info("OrderService processOrder: Method started for userId " + userId + ", total " + total + ", address: " + deliveryAddress);

        // 1. Validate stock (re-check before final processing)
        for (CartItem item : cartItems) {
            if (item == null || item.getProduct() == null) {
                LOGGER.severe("OrderService processOrder: CartItem or its Product is null during stock validation.");
                return null;
            }
            LOGGER.info("OrderService processOrder: Validating stock for product ID " + item.getProduct().getId() + " quantity " + item.getQuantity());
            if (!productDAO.isAvailable(item.getProduct().getId(), item.getQuantity())) {
                System.err.println("Order processing failed: Insufficient stock for product " + item.getProduct().getName());
                LOGGER.warning("OrderService processOrder: Insufficient stock for product " + item.getProduct().getName());
                return null;
            }
        }
        LOGGER.info("OrderService processOrder: Stock validation passed.");

        // 2. Create Order object
        Order order = new Order();
        order.setUserId(userId);
        order.setTotal(total);
        order.setStatus("completed");
        order.setDeliveryAddress(deliveryAddress); // NEW: Set delivery address
        LOGGER.info("OrderService processOrder: Order object created: " + order.toString());

        // 3. Save order and order items to database (transactionally)
        Order savedOrder = orderDAO.saveOrder(order, cartItems);

        if (savedOrder != null) {
            LOGGER.info("OrderService processOrder: Order saved successfully. Returning saved order.");
            return savedOrder;
        }
        LOGGER.severe("OrderService processOrder: OrderDAO.saveOrder failed, returning null.");
        return null;
    }

    /**
     * Retrieves all orders for a given user, including their items.
     * @param userId The ID of the user.
     * @return A list of orders with populated items.
     */
    public List<Order> getOrdersByUserId(int userId) {
        LOGGER.info("OrderService getOrdersByUserId: Fetching orders for user ID: " + userId);
        List<Order> orders = orderDAO.getOrdersByUserId(userId); // This now fetches items
        LOGGER.info("OrderService getOrdersByUserId: Retrieved " + orders.size() + " orders for user ID: " + userId + " with items.");
        return orders;
    }

    /**
     * Searches orders by Order ID or product name within a user's orders.
     * @param userId The ID of the user whose orders to search.
     * @param searchTerm The term to search for (Order ID or product name).
     * @return A list of matching orders.
     */
    public List<Order> searchOrders(int userId, String searchTerm) { // NEW Method
        LOGGER.info("OrderService searchOrders: Searching orders for user ID: " + userId + " with term: " + searchTerm);
        List<Order> foundOrders = orderDAO.searchOrders(userId, searchTerm);
        LOGGER.info("OrderService searchOrders: Found " + foundOrders.size() + " orders.");
        return foundOrders;
    }
}
