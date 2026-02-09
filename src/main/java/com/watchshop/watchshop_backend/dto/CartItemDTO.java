package com.watchshop.watchshop_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDTO {

    @JsonProperty("productId")
    private Long productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("price")
    private Double price;

    public CartItemDTO(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // ✅ REQUIRED CONSTRUCTOR (THIS FIXES ERROR)
    public CartItemDTO(
            Long productId,
            String productName,
            Integer quantity,
            Double price
    ) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // empty constructor (Jackson ke liye)
    public CartItemDTO() {}

    // getters & setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
