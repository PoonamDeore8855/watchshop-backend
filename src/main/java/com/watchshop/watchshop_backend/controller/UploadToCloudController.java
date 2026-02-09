package com.watchshop.watchshop_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.watchshop.watchshop_backend.service.ImageUploadService;

@RestController
@RequestMapping("/image")
@CrossOrigin(origins = "http://localhost:5174")
public class UploadToCloudController {

    private final ImageUploadService imageUploadService;

    // ✅ Constructor injection
    public UploadToCloudController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    // ✅ IMAGE UPLOAD API
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file) {

        String imageUrl = imageUploadService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }
}
