package com.techstore.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Added for LocalDateTime
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // Added for GsonBuilder
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer; // Added for GsonBuilder
import com.google.gson.JsonElement; // Added for GsonBuilder
import com.google.gson.JsonPrimitive; // Added for GsonBuilder
import com.google.gson.JsonSerializationContext; // Added for GsonBuilder
import com.google.gson.JsonSerializer; // Added for GsonBuilder
import com.techstore.model.Product;
import com.techstore.service.ProductService;

/**
 * ProductServlet handles all product-related API requests
 * Endpoints:
 * GET /api/products - Get all products
 * GET /api/products/{id} - Get product by ID
 * POST /api/products - Add new product (admin only)
 * PUT /api/products/{id} - Update product (admin only)
 * DELETE /api/products/{id} - Delete product (admin only)
 * Now interacts with ProductService, which uses ProductDAO.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/api/products", "/api/products/*"})
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private Gson gson;
    private static final Logger LOGGER = Logger.getLogger(ProductServlet.class.getName());

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
     * Helper method to send error response with JSON format
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        String errorJson = "{\"error\": \"" + message + "\", \"status\": " + status + "}";
        out.print(errorJson);
        out.close();
    }

    /**
     * Handle GET requests for products
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null; // Declare out here
        try {
            out = response.getWriter(); // Initialize out here

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                String searchTerm = request.getParameter("search");
                String minPriceStr = request.getParameter("minPrice");
                String maxPriceStr = request.getParameter("maxPrice");

                List<Product> products;

                if (searchTerm != null && !searchTerm.isEmpty()) {
                    products = productService.searchProducts(searchTerm);
                } else if (minPriceStr != null && maxPriceStr != null) {
                    try {
                        BigDecimal minPrice = new BigDecimal(minPriceStr);
                        BigDecimal maxPrice = new BigDecimal(maxPriceStr);
                        products = productService.getProductsByPriceRange(minPrice, maxPrice);
                    } catch (NumberFormatException e) {
                        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Invalid price format");
                        return;
                    }
                } else {
                    products = productService.getAllProducts();
                    LOGGER.info("ProductServlet doGet: Fetched " + products.size() + " products from DB.");
                }

                String json = gson.toJson(products); // This is line 49
                out.print(json);

            } else {
                // Get product by ID
                try {
                    int productId = Integer.parseInt(pathInfo.substring(1));
                    Optional<Product> product = productService.getProductById(productId);

                    if (product.isPresent()) {
                        String json = gson.toJson(product.get());
                        out.print(json);
                    } else {
                        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                                "Product not found");
                        return;
                    }

                } catch (NumberFormatException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid product ID");
                    return;
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Server error in ProductServlet doGet: " + e.getMessage());
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
     * Handle POST requests for creating new products
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

            Product product = gson.fromJson(sb.toString(), Product.class);

            if (product.getName() == null || product.getName().isEmpty() ||
                product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product data: Name and positive price are required.");
                return;
            }

            Product savedProduct = productService.addProduct(product);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(savedProduct));

        } catch (Exception e) {
            LOGGER.severe("Server error in ProductServlet doPost: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error adding product: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Handle PUT requests for updating products
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Product ID is required for update.");
                return;
            }

            int productId = Integer.parseInt(pathInfo.substring(1));

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Product product = gson.fromJson(sb.toString(), Product.class);
            product.setId(productId);

            if (product.getName() == null || product.getName().isEmpty() ||
                product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid updated product data: Name and positive price are required.");
                return;
            }

            boolean updated = productService.updateProduct(product);

            if (updated) {
                out.print(gson.toJson(product));
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Product not found for update.");
            }

        } catch (Exception e) {
            LOGGER.severe("Server error in ProductServlet doPut: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error updating product: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Handle DELETE requests for removing products
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = null;
        try {
            out = response.getWriter();

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Product ID is required for deletion.");
                return;
            }

            int productId = Integer.parseInt(pathInfo.substring(1));
            boolean deleted = productService.deleteProduct(productId);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Product not found for deletion.");
            }

        } catch (Exception e) {
            LOGGER.severe("Server error in ProductServlet doDelete: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error deleting product: " + e.getMessage());
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
