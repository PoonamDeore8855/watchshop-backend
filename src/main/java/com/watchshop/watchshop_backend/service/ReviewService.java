package com.watchshop.watchshop_backend.service;

import com.watchshop.watchshop_backend.dto.ReviewRequestDTO;
import com.watchshop.watchshop_backend.dto.ReviewResponseDTO;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.entity.Review;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import com.watchshop.watchshop_backend.repository.ReviewRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public ReviewResponseDTO addReview(ReviewRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        return mapToResponseDTO(savedReview);
    }

    public List<ReviewResponseDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO mapToResponseDTO(Review review) {
        ReviewResponseDTO response = new ReviewResponseDTO();
        response.setId(review.getId());
        response.setUsername(review.getUser().getUsername());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
