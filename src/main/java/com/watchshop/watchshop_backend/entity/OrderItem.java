package com.watchshop.watchshop_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;
    private Double price;

    // 🔗 many items → one order
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    // 🔗 many items → one product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // getters & setters
    public Long getId() { return id; }
    public Integer getQuantity() { return quantity; }
    public Double getPrice() { return price; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setPrice(Double price) { this.price = price; }
    public void setOrder(Order order) { this.order = order; }
    public void setProduct(Product product) { this.product = product; }
}
