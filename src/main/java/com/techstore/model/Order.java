package com.techstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Import for List<OrderItem>
import java.util.Objects;

/**
 * Order model class representing a customer order
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId; // 0 or null for guest users
    private BigDecimal total;
    private String status; // e.g., "pending", "completed", "cancelled"
    private LocalDateTime createdAt;
    private String deliveryAddress; // NEW: Added delivery address field
    private List<OrderItem> orderItems; // NEW: List of items in this order

    public Order() {
        this.status = "pending"; // Default status
        this.createdAt = LocalDateTime.now();
    }

    public Order(int userId, BigDecimal total, String status, String deliveryAddress) { // NEW Constructor
        this.userId = userId;
        this.total = total;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getDeliveryAddress() { return deliveryAddress; } // NEW Getter
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; } // NEW Setter
    public List<OrderItem> getOrderItems() { return orderItems; } // NEW Getter
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; } // NEW Setter

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", total=" + total +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", orderItems size=" + (orderItems != null ? orderItems.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
