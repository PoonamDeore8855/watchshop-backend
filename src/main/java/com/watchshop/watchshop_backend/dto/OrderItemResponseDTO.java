package com.watchshop.watchshop_backend.dto;

public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private String imageUrl;

    public OrderItemResponseDTO(Long id,
                                Long productId,
                                String productName,
                                Integer quantity,
                                Double price,
                                String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
