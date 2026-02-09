package com.watchshop.watchshop_backend.service;

import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final ImageUploadService imageUploadService;

    public ProductService(ProductRepository repository, ImageUploadService imageUploadService) {
        this.repository = repository;
        this.imageUploadService = imageUploadService;
    }

    // ✅ USER / NORMAL PRODUCT SAVE (JSON)
    public Product save(Product p) {

        Product product = new Product();
        product.setName(p.getName());
        product.setBrand(p.getBrand());
        product.setPrice(p.getPrice());
        product.setStock(p.getStock() != null ? p.getStock() : 10);
        product.setImageUrl(p.getImageUrl());

        return repository.save(product);
    }

    // ✅ ADMIN PRODUCT SAVE (WITH IMAGE)
    public Product addProduct(
            String name,
            double price,
            String brand,
            MultipartFile image
    ) {
        Product product = new Product();

        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setStock(10);

        if (image != null && !image.isEmpty()) {
            String url = imageUploadService.uploadImage(image);
            product.setImageUrl(url);
        }

        return repository.save(product);
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Product updateProduct(Long id, Product details) {
        Product product = getById(id);
        product.setName(details.getName());
        product.setBrand(details.getBrand());
        product.setPrice(details.getPrice());
        product.setStock(details.getStock());
        if (details.getImageUrl() != null) {
            product.setImageUrl(details.getImageUrl());
        }
        return repository.save(product);
    }
}
