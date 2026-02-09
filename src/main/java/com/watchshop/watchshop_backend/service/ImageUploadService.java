package com.watchshop.watchshop_backend.service;

import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageUploadService {

    @Value("${imagekit.privateKey}")
    private String privateKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String uploadImage(MultipartFile file) {

        try {
            String uploadUrl = "https://upload.imagekit.io/api/v1/files/upload";

            String auth = privateKey + ":";
            String encodedAuth =
                    Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            String base64File =
                    "data:" + file.getContentType() + ";base64," +
                    Base64.getEncoder().encodeToString(file.getBytes());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("file", base64File);
            body.add("fileName", file.getOriginalFilename());
            body.add("folder", "watchshop");

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(uploadUrl, request, String.class);

            JsonNode jsonNode =
                    objectMapper.readTree(response.getBody());

            return jsonNode.get("url").asText();

        } catch (Exception e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }
}
