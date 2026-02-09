package com.watchshop.watchshop_backend.config;

import com.watchshop.watchshop_backend.entity.Admin;
import com.watchshop.watchshop_backend.entity.Product;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.repository.AdminRepository;
import com.watchshop.watchshop_backend.repository.ProductRepository;
import com.watchshop.watchshop_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepo, 
                                      AdminRepository adminRepo,
                                      ProductRepository productRepo,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            
            // 1. Seed User if empty
            if (userRepo.count() == 0) {
                User admin = new User();
                admin.setUsername("Admin User");
                admin.setEmail("admin@watchshop.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN"); // JwtAuthenticationFilter adds "ROLE_"
                userRepo.save(admin);
                System.out.println("✅ Seeded Admin User: admin@watchshop.com / admin123");

                User user = new User();
                user.setUsername("Demo User");
                user.setEmail("user@watchshop.com");
                user.setPassword(passwordEncoder.encode("user123")); 
                user.setRole("USER");
                userRepo.save(user);
                System.out.println("✅ Seeded Demo User: user@watchshop.com / user123");
            }

            // 1.5 Seed Admin if empty
            if (adminRepo.findByEmail("admin@watchshop.com").isEmpty()) {
                Admin adminEntity = new Admin();
                adminEntity.setEmail("admin@watchshop.com");
                adminEntity.setPassword(passwordEncoder.encode("admin123"));
                adminRepo.save(adminEntity);
                System.out.println("✅ Seeded Admin Entity: admin@watchshop.com / admin123");
            }

            // 2. Seed Products if empty
            if (productRepo.count() == 0) {
                Product p1 = new Product();
                p1.setName("Luxury Gold Watch");
                p1.setPrice(12500.0);
                p1.setBrand("Rolex");
                p1.setImageUrl("https://images.unsplash.com/photo-1524592094714-0f0654e20314?auto=format&fit=crop&w=500&q=60");

                Product p2 = new Product();
                p2.setName("Classic Leather");
                p2.setPrice(8500.0);
                p2.setBrand("Tissot");
                p2.setImageUrl("https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?auto=format&fit=crop&w=500&q=60");

                Product p3 = new Product();
                p3.setName("Ocean Diver");
                p3.setPrice(15000.0);
                p3.setBrand("Omega");
                p3.setImageUrl("https://images.unsplash.com/photo-1547996160-81dfa63595dd?auto=format&fit=crop&w=500&q=60");

                Product p4 = new Product();
                p4.setName("Aviation Pro");
                p4.setPrice(18200.0);
                p4.setBrand("Breitling");
                p4.setImageUrl("https://images.unsplash.com/photo-1526045431048-f857369aba09?auto=format&fit=crop&w=500&q=60");

                productRepo.saveAll(Arrays.asList(p1, p2, p3, p4));
                System.out.println("✅ Seeded 4 Products.");
            }
        };
    }
}
