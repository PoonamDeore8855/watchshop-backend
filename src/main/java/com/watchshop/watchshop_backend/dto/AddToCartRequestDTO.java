package com.watchshop.watchshop_backend.dto;

import lombok.Data;

@Data
public class AddToCartRequestDTO {
    private Long productId;
    private int quantity;
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
    

   
}
