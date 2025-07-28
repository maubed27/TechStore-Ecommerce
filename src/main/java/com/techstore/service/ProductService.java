package com.techstore.service;

import java.math.BigDecimal;
import java.util.ArrayList; // Keep if search/filter is done in-memory after fetching all
import java.util.List;
import java.util.Optional;

import com.techstore.dao.ProductDAO; // Import the new ProductDAO
import com.techstore.model.Product;

/**
 * ProductService class to manage product operations.
 * Now interacts with ProductDAO for database persistence.
 */
public class ProductService {
    private static ProductService instance; // Singleton pattern
    private ProductDAO productDAO; // Use DAO instead of in-memory list

    private ProductService() {
        this.productDAO = new ProductDAO(); // Initialize the DAO
        // No need for initializeProducts() here anymore, data comes from DB
    }

    public static ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }

    // All methods now delegate to ProductDAO
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public Optional<Product> getProductById(int id) {
        return productDAO.getProductById(id);
    }

    public Product addProduct(Product product) {
        return productDAO.addProduct(product);
    }

    public boolean updateProduct(Product updatedProduct) {
        return productDAO.updateProduct(updatedProduct);
    }

    public boolean deleteProduct(int id) {
        return productDAO.deleteProduct(id);
    }

    // This method might not be directly used if reduceStock is preferred for stock changes
    public boolean updateStock(int productId, int newStock) {
        Optional<Product> productOpt = productDAO.getProductById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);
            return productDAO.updateProduct(product);
        }
        return false;
    }

    public boolean isAvailable(int productId, int quantity) {
        return productDAO.isAvailable(productId, quantity);
    }

    public boolean reduceStock(int productId, int quantity) {
        return productDAO.reduceStock(productId, quantity);
    }

    // These methods now delegate to ProductDAO's search/filter methods
    public List<Product> searchProducts(String searchTerm) {
        return productDAO.searchProducts(searchTerm);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productDAO.getProductsByPriceRange(minPrice, maxPrice);
    }

    // This method is not directly implemented in ProductDAO yet, so it fetches all and filters in-memory
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> allProducts = productDAO.getAllProducts();
        List<Product> lowStockProducts = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getStock() <= threshold) {
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }
}
