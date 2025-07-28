package com.techstore.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.techstore.model.CartItem;
import com.techstore.model.Product;
import com.techstore.service.ProductService;

/**
 * CartServlet handles all shopping cart-related API requests
 * Endpoints:
 * GET /api/cart - Get cart contents
 * POST /api/cart - Add item to cart
 * PUT /api/cart - Update item quantity
 * DELETE /api/cart/{productId} - Remove item from cart
 * DELETE /api/cart - Clear entire cart
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartServlet extends HttpServlet {

    private ProductService productService;
    private Gson gson;
    private static final String CART_SESSION_KEY = "cart";
    private static final Logger LOGGER = Logger.getLogger(CartServlet.class.getName());

    @Override
    public void init() throws ServletException {
        productService = ProductService.getInstance();
        // Configure Gson to handle LocalDateTime using the custom adapter
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
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

    /**
     * Helper method to get cart from session, creating if null
     */
    @SuppressWarnings("unchecked") // Suppress unchecked cast warning for session.getAttribute
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    /**
     * Handle GET requests for cart contents
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            HttpSession session = request.getSession();
            List<CartItem> cart = getCartFromSession(session);

            // Calculate cart summary
            int totalItems = cart.stream().mapToInt(CartItem::getQuantity).sum();
            BigDecimal totalAmount = cart.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CartResponse cartResponse = new CartResponse(cart, totalItems, totalAmount);
            String json = gson.toJson(cartResponse);
            LOGGER.info("CartServlet doGet sending JSON response: " + json);
            out.print(json);

        } catch (Exception e) {
            LOGGER.severe("Server error in CartServlet doGet: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Handle POST requests for adding items to cart
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Suppress warnings for raw type Map.class
            @SuppressWarnings("rawtypes")
            AddToCartRequest addRequest = gson.fromJson(sb.toString(), AddToCartRequest.class);

            if (addRequest == null || addRequest.productId <= 0 || addRequest.quantity <= 0) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID or quantity");
                return;
            }

            // Get product details from DB via ProductService
            Optional<Product> productOpt = productService.getProductById(addRequest.productId);
            if (!productOpt.isPresent()) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Product not found");
                return;
            }

            Product product = productOpt.get();

            // Add to cart
            HttpSession session = request.getSession();
            List<CartItem> cart = getCartFromSession(session);

            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProduct().getId() == addRequest.productId)
                    .findFirst();

            if (existingItem.isPresent()) {
                // Update existing item quantity
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + addRequest.quantity;

                // Check stock availability for the *total* new quantity
                if (!productService.isAvailable(addRequest.productId, newQuantity)) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Not enough stock for desired total quantity: " + product.getName());
                    return;
                }
                item.setQuantity(newQuantity);
            } else {
                // Check stock availability for the initial quantity for a new item
                if (!productService.isAvailable(addRequest.productId, addRequest.quantity)) {
                     sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Not enough stock for initial quantity: " + product.getName());
                    return;
                }
                cart.add(new CartItem(product, addRequest.quantity));
            }

            response.setStatus(HttpServletResponse.SC_OK);
            // Recalculate and send updated cart response, similar to doGet
            int totalItems = cart.stream().mapToInt(CartItem::getQuantity).sum();
            BigDecimal totalAmount = cart.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CartResponse cartResponse = new CartResponse(cart, totalItems, totalAmount);
            String jsonToReturn = gson.toJson(cartResponse);
            LOGGER.info("CartServlet doPost sending JSON response: " + jsonToReturn);
            out.print(jsonToReturn);

        } catch (NumberFormatException e) {
            LOGGER.severe("NumberFormatException in CartServlet doPost: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID or quantity format in request.");
        } catch (Exception e) {
            LOGGER.severe("Server error in CartServlet doPost: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error adding to cart: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Handle PUT requests for updating item quantity in cart
     * Expects JSON: {"productId": 1, "quantity": 5}
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Suppress warnings for raw type Map.class
            @SuppressWarnings("rawtypes")
            Map<String, Object> requestData = gson.fromJson(sb.toString(), Map.class);
            int productId = ((Double) requestData.get("productId")).intValue();
            int quantity = ((Double) requestData.get("quantity")).intValue();

            if (productId <= 0 || quantity < 0) { // Quantity can be 0 to remove item
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID or quantity");
                return;
            }

            HttpSession session = request.getSession();
            List<CartItem> cart = getCartFromSession(session);

            Optional<CartItem> itemOpt = cart.stream()
                    .filter(item -> item.getProduct().getId() == productId)
                    .findFirst();

            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                if (quantity > 0) {
                    // Check stock availability for the new quantity
                    if (!productService.isAvailable(productId, quantity)) {
                        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Not enough stock for desired quantity: " + item.getProduct().getName());
                        return;
                    }
                    item.setQuantity(quantity);
                } else { // quantity is 0 or less, remove the item
                    cart.remove(item);
                }

                response.setStatus(HttpServletResponse.SC_OK);
                // Recalculate and send updated cart response
                int totalItems = cart.stream().mapToInt(CartItem::getQuantity).sum();
                BigDecimal totalAmount = cart.stream()
                        .map(CartItem::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                CartResponse cartResponse = new CartResponse(cart, totalItems, totalAmount);
                String jsonToReturn = gson.toJson(cartResponse);
                LOGGER.info("CartServlet doPut sending JSON response: " + jsonToReturn);
                out.print(jsonToReturn);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Item not found in cart");
            }

        } catch (NumberFormatException e) {
            LOGGER.severe("NumberFormatException in CartServlet doPut: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID or quantity format.");
        } catch (Exception e) {
            LOGGER.severe("Server error in CartServlet doPut: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error updating cart: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Handle DELETE requests for removing items from cart or clearing the whole cart
     * DELETE /api/cart/{productId} - Remove specific item
     * DELETE /api/cart - Clear entire cart
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.length() > 1) {
                // Remove specific item by ID
                String[] pathParts = pathInfo.split("/");
                int productId = Integer.parseInt(pathParts[1]);

                List<CartItem> cart = getCartFromSession(request.getSession()); // Get cart explicitly here

                boolean removed = false;
                if (cart != null) {
                    removed = cart.removeIf(item -> item.getProduct().getId() == productId);
                }

                if (removed) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    // Recalculate and send updated cart response
                    int totalItems = cart.stream().mapToInt(CartItem::getQuantity).sum();
                    BigDecimal totalAmount = cart.stream()
                            .map(CartItem::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    CartResponse cartResponse = new CartResponse(cart, totalItems, totalAmount);
                    String jsonToReturn = gson.toJson(cartResponse);
                    LOGGER.info("CartServlet doDelete (item) sending JSON response: " + jsonToReturn);
                    out.print(jsonToReturn);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                            "Item not found in cart for removal.");
                    LOGGER.warning("Cart item with ID " + productId + " not found for removal.");
                }
            } else {
                // Clear entire cart
                List<CartItem> cart = getCartFromSession(request.getSession());
                if (cart != null) {
                    cart.clear();
                }
                response.setStatus(HttpServletResponse.SC_OK);
                String jsonToReturn = "{\"message\":\"Cart cleared successfully!\"}";
                LOGGER.info("CartServlet doDelete (clear cart) sending JSON response: " + jsonToReturn);
                out.print(jsonToReturn);
            }

        } catch (NumberFormatException e) {
            LOGGER.severe("NumberFormatException in CartServlet doDelete: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID format in request.");
        } catch (Exception e) {
            LOGGER.severe("Server error in CartServlet doDelete: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error removing from cart: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Inner class to represent the structure of the JSON request for adding to cart.
     */
    private static class AddToCartRequest {
        int productId;
        int quantity;
    }

    /**
     * Inner class to represent the structure of the JSON response for cart contents.
     */
    private static class CartResponse {
        List<CartItem> cartItems;
        int totalItems;
        BigDecimal totalAmount;

        public CartResponse(List<CartItem> cartItems, int totalItems, BigDecimal totalAmount) {
            this.cartItems = cartItems;
            this.totalItems = totalItems;
            this.totalAmount = totalAmount;
        }
    }

    /**
     * Custom LocalDateTime adapter for Gson
     * Needed because Gson doesn't handle Java 8 Date/Time API by default via reflection.
     */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}
