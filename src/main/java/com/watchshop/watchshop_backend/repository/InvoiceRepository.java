package com.watchshop.watchshop_backend.repository;

import com.watchshop.watchshop_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);

    List<Invoice> findByUserIdOrderByGeneratedAtDesc(Long userId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE DATE(i.generatedAt) = :date")
    Long countByDate(LocalDate date);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
