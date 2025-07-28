// --- src/main/java/com/techstore/model/CartItem.java ---
package com.techstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * CartItem model class representing an item in the shopping cart
 */
public class CartItem implements Serializable { // Implements Serializable
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private Product product;
    private int quantity;

    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() {
        if (product != null && product.getPrice() != null) {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getPrice() { // Added this getter for unit price
        if (product != null && product.getPrice() != null) {
            return product.getPrice();
        }
        return BigDecimal.ZERO;
    }


    @Override
    public String toString() {
        return "CartItem{" + "product=" + (product != null ? product.getName() : "N/A") + ", quantity=" + quantity + ", subtotal=" + getSubtotal() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return this.product != null && cartItem.product != null && this.product.getId() == cartItem.product.getId();
    }

    @Override
    public int hashCode() { return Objects.hash(product != null ? product.getId() : 0); }
}
