package com.techstore.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
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
import com.techstore.model.Order;
import com.techstore.model.User;
import com.techstore.service.OrderService;
import com.techstore.service.UserService;

@WebServlet("/api/user/*")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson;
    private UserService userService;
    private OrderService orderService;
    private static final Logger LOGGER = Logger.getLogger(UserServlet.class.getName());

    @Override
    public void init() throws ServletException {
        userService = UserService.getInstance();
        orderService = OrderService.getInstance();
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
     * Helper method to send error response with JSON format
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

            String pathInfo = request.getPathInfo();

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            @SuppressWarnings("unchecked")
            Map<String, String> requestData = gson.fromJson(sb.toString(), Map.class);

            if ("/login".equals(pathInfo)) {
                handleLogin(request, response, requestData, out);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(request, response, requestData, out);
            } else if ("/logout".equals(pathInfo)) {
                handleLogout(request, response, out);
            } else if ("/status".equals(pathInfo)) {
                handleStatus(request, response, out);
            }
            else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Endpoint not found for /api/user" + pathInfo);
            }

        } catch (Exception e) {
            LOGGER.severe("Server error in UserServlet doPost: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request data or server error: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response,
                           Map<String, String> requestData, PrintWriter out) throws IOException {
        String email = requestData.get("email");
        String password = requestData.get("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
            return;
        }

        Optional<User> userOpt = userService.login(email, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userId", user.getId());
            session.setMaxInactiveInterval(30 * 60);

            Map<String, Object> responseData = Map.of(
                "success", true,
                "message", "Login successful",
                "user", user.getEmail(),
                "userId", user.getId()
            );
            out.print(gson.toJson(responseData));
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response,
                              Map<String, String> requestData, PrintWriter out) throws IOException {
        String email = requestData.get("email");
        String password = requestData.get("password");
        String firstName = requestData.get("firstName");
        String lastName = requestData.get("lastName");

        if (email == null || email.isEmpty() || password == null || password.isEmpty() ||
            firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "All fields (email, password, first name, last name) are required for registration.");
            return;
        }

        User newUser = userService.register(email, password, firstName, lastName);

        if (newUser != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", newUser.getEmail());
            session.setAttribute("userId", newUser.getId());

            Map<String, Object> responseData = Map.of(
                "success", true,
                "message", "Registration successful",
                "user", newUser.getEmail(),
                "userId", newUser.getId()
            );
            out.print(gson.toJson(responseData));
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "User with this email already exists.");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response,
                            PrintWriter out) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        out.print("{\"success\":true,\"message\":\"Logged out successfully\"}");
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response,
                              PrintWriter out) throws IOException {
        HttpSession session = request.getSession(false);
        String userEmail = (session != null) ? (String) session.getAttribute("userEmail") : null;
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        Map<String, Object> responseData = new HashMap<>();
        if (userEmail != null && userId != null) {
            responseData.put("loggedIn", true);
            responseData.put("userEmail", userEmail);
            responseData.put("userId", userId);
        } else {
            responseData.put("loggedIn", false);
        }
        out.print(gson.toJson(responseData));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if ("/orders".equals(pathInfo)) {
            handleUserOrders(request, response);
        } else if ("/status".equals(pathInfo)) {
            handleStatus(request, response, response.getWriter());
        } else if ("/searchorders".equals(pathInfo)) { // NEW: Handle order search
            handleSearchOrders(request, response);
        }
        else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found for GET /api/user" + pathInfo);
        }
    }

    private void handleUserOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to view your orders.");
                return;
            }

            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                 sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User ID not found in session. Please re-login.");
                 return;
            }

            List<Order> orders = orderService.getOrdersByUserId(userId); // This now fetches orders with items
            out.print(gson.toJson(orders));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching user orders: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    // NEW: Handle order search method
    private void handleSearchOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to search orders.");
                return;
            }

            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User ID not found in session. Please re-login.");
                return;
            }

            String searchTerm = request.getParameter("searchTerm"); // Get search term from query parameter
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Search term is required.");
                return;
            }

            List<Order> orders = orderService.searchOrders(userId, searchTerm);
            out.print(gson.toJson(orders));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error searching orders: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Custom LocalDateTime adapter for Gson
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
