package com.watchshop.watchshop_backend.controller;

import com.watchshop.watchshop_backend.entity.Invoice;
import com.watchshop.watchshop_backend.entity.User;
import com.watchshop.watchshop_backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    /**
     * Get invoice by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getInvoiceByOrderId(@PathVariable Long orderId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Invoice> invoice = invoiceService.getInvoiceByOrderId(orderId);

            if (invoice.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Invoice not found"));
            }

            // Security: Ensure user owns this invoice
            if (!invoice.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Unauthorized access"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", invoice.get().getId());
            response.put("invoiceNumber", invoice.get().getInvoiceNumber());
            response.put("orderId", invoice.get().getOrder().getId());
            response.put("generatedAt", invoice.get().getGeneratedAt());
            response.put("subtotal", invoice.get().getSubtotal());
            response.put("tax", invoice.get().getTax());
            response.put("grandTotal", invoice.get().getGrandTotal());
            response.put("paymentMethod", invoice.get().getPaymentMethod());
            response.put("paymentStatus", invoice.get().getPaymentStatus());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch invoice", "message", e.getMessage()));
        }
    }

    /**
     * Get all invoices for logged-in user
     */
    @GetMapping("/user/all")
    public ResponseEntity<?> getUserInvoices(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<Invoice> invoices = invoiceService.getUserInvoices(user.getId());

            List<Map<String, Object>> response = invoices.stream().map(inv -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", inv.getId());
                map.put("invoiceNumber", inv.getInvoiceNumber());
                map.put("orderId", inv.getOrder().getId());
                map.put("generatedAt", inv.getGeneratedAt());
                map.put("grandTotal", inv.getGrandTotal());
                map.put("paymentStatus", inv.getPaymentStatus());
                return map;
            }).toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch invoices", "message", e.getMessage()));
        }
    }

    /**
     * Download invoice PDF
     */
    @GetMapping("/{invoiceId}/download")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long invoiceId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Invoice> invoiceOpt = invoiceService.getInvoiceByOrderId(invoiceId);

            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Invoice invoice = invoiceOpt.get();

            // Security: Ensure user owns this invoice
            if (!invoice.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            File file = new File(invoice.getPdfPath());
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + invoice.getInvoiceNumber() + ".pdf\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download invoice by order ID
     */
    @GetMapping("/order/{orderId}/download")
    public ResponseEntity<Resource> downloadInvoiceByOrderId(@PathVariable Long orderId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Invoice> invoiceOpt = invoiceService.getInvoiceByOrderId(orderId);

            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Invoice invoice = invoiceOpt.get();

            // Security: Ensure user owns this invoice
            if (!invoice.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            File file = new File(invoice.getPdfPath());
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + invoice.getInvoiceNumber() + ".pdf\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
