package com.watchshop.watchshop_backend.controller;

import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // 👤 USER ADD PRODUCT (JSON)
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return service.save(product);
    }

    // 👤 USER GET PRODUCTS
    @GetMapping("/get")
    public List<Product> getAll() {
        return service.getAll();
    }

    // 👑 ADMIN DELETE PRODUCT
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteById(id);
    }
}
