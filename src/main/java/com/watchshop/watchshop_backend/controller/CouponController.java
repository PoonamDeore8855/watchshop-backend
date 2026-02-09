package com.watchshop.watchshop_backend.controller;

import com.watchshop.watchshop_backend.entity.Coupon;
import com.watchshop.watchshop_backend.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "http://localhost:5173")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code.toUpperCase());

        if (couponOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid promo code.");
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.isActive()) {
            return ResponseEntity.badRequest().body("This promo code is no longer active.");
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("This promo code has expired.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", coupon.getCode());
        response.put("discountPercentage", coupon.getDiscountPercentage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Coupon> addCoupon(@RequestBody Coupon coupon) {
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase());
        }
        if (coupon.getExpiryDate() == null) {
            coupon.setExpiryDate(LocalDateTime.now().plusMonths(1));
        }
        coupon.setActive(true);
        return ResponseEntity.ok(couponRepository.save(coupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 🌱 SEED DATA (Temporary for testing)
    @PostMapping("/seed")
    public ResponseEntity<String> seedCoupons() {
        if (couponRepository.findByCode("SAVE10").isPresent()) {
            return ResponseEntity.ok("Seed data already exists.");
        }

        Coupon c1 = new Coupon();
        c1.setCode("SAVE10");
        c1.setDiscountPercentage(10.0);
        c1.setExpiryDate(LocalDateTime.now().plusMonths(1));
        c1.setActive(true);
        couponRepository.save(c1);

        Coupon c2 = new Coupon();
        c2.setCode("LUXURY20");
        c2.setDiscountPercentage(20.0);
        c2.setExpiryDate(LocalDateTime.now().plusMonths(2));
        c2.setActive(true);
        couponRepository.save(c2);

        return ResponseEntity.ok("Test coupons 'SAVE10' and 'LUXURY20' created successfully!");
    }
}
