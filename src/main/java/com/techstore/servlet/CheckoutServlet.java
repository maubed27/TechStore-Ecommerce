package com.techstore.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.techstore.model.CartItem;
import com.techstore.model.Order;
import com.techstore.service.OrderService;
import com.techstore.service.ProductService; // Keep for ProductService.getInstance() in init, though not directly used in doPost

@WebServlet("/api/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductService productService; // ProductService is used by OrderService for stock validation
    private OrderService orderService;
    private Gson gson;
    private static final Logger LOGGER = Logger.getLogger(CheckoutServlet.class.getName());

    @Override
    public void init() throws ServletException {
        productService = ProductService.getInstance(); // Ensure ProductService is initialized (used by OrderService internally)
        orderService = OrderService.getInstance();
        gson = new Gson();
    }

    /**
     * Helper method to set JSON response headers
     */
    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    /**
     * Helper method to send error response with JSON format.
     * This method does NOT close the PrintWriter, allowing the calling method's finally block to handle it.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        String errorJson = "{\"error\": \"" + message + "\", \"status\": " + status + "}";
        out.print(errorJson);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            HttpSession session = request.getSession(false);
            if (session == null) {
                LOGGER.warning("Checkout attempt with no active session.");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "No active session. Please log in.");
                return;
            }

            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                userId = 0; // Use 0 for guest checkout if no user is logged in
            }

            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

            if (cart == null || cart.isEmpty()) {
                LOGGER.warning("Checkout attempt with empty cart.");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Cart is empty. Add items before checking out.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString(); // Store request body for logging
            LOGGER.info("CheckoutServlet received request body: " + requestBody);
            
            @SuppressWarnings("unchecked")
            Map<String, String> customerInfo = gson.fromJson(requestBody, Map.class);
            String deliveryAddress = customerInfo.get("deliveryAddress"); // NEW: Get delivery address
            if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
                deliveryAddress = "Not Provided"; // Default for now if empty
            }
            LOGGER.info("CheckoutServlet parsed delivery address: " + deliveryAddress);


            BigDecimal total = cart.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Pass deliveryAddress to processOrder
            Order createdOrder = orderService.processOrder(cart, userId, total, deliveryAddress); // MODIFIED CALL

            if (createdOrder != null) {
                session.removeAttribute("cart");
                LOGGER.info("Cart cleared from session after successful checkout. Order ID: " + createdOrder.getId());

                Map<String, Object> response_data = Map.of(
                    "success", true,
                    "orderId", createdOrder.getId(),
                    "total", createdOrder.getTotal(),
                    "message", "Order placed successfully! Thank you for your purchase."
                );

                String jsonToReturn = gson.toJson(response_data);
                LOGGER.info("CheckoutServlet sending JSON response: " + jsonToReturn);
                out.print(jsonToReturn);
            } else {
                LOGGER.severe("Order processing failed in OrderService (returned null).");
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Order processing failed. Please try again.");
            }

        } catch (Exception e) {
            LOGGER.severe("Checkout failed due to server error: " + e.getMessage());
            e.printStackTrace(); // Print stack trace to console
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Checkout failed due to server error: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
