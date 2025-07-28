package com.techstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * OrderItem model class representing a single product item within a customer's order.
 * Stores details at the time of order for historical accuracy.
 */
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int orderId;
    private int productId; // ID of the product at the time of order
    private String productName; // Name of the product at the time of order
    private int quantity;
    private BigDecimal price; // Price per unit at the time of order
    private String productImage; // Image URL/path at the time of order (for display in history)

    public OrderItem() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getProductImage() { return productImage; } // NEW Getter for image
    public void setProductImage(String productImage) { this.productImage = productImage; } // NEW Setter for image

    /**
     * Calculates the subtotal for this order item.
     * @return The subtotal.
     */
    public BigDecimal getSubtotal() {
        if (price != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", productImage='" + productImage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id == orderItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
