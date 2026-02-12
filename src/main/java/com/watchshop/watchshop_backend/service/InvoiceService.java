package com.watchshop.watchshop_backend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.watchshop.watchshop_backend.entity.Invoice;
import com.watchshop.watchshop_backend.entity.Order;
import com.watchshop.watchshop_backend.entity.OrderItem;
import com.watchshop.watchshop_backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Value("${invoice.storage.path:invoices}")
    private String invoiceStoragePath;

    private static final Double GST_RATE = 0.18; // 18%

    /**
     * Generate invoice for an order
     */
    @Async
    public void generateInvoice(Order order) throws Exception {
        // Check if invoice already exists
        Optional<Invoice> existing = invoiceRepository.findByOrderId(order.getId());
        if (existing.isPresent()) {
            return;
        }

        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setOrder(order);
        invoice.setUser(order.getUser());

        // Calculate amounts
        Double subtotal = order.getTotalAmount();
        Double tax = subtotal * GST_RATE;
        Double grandTotal = subtotal + tax;

        invoice.setSubtotal(subtotal);
        invoice.setTax(tax);
        invoice.setDiscount(0.0);
        invoice.setGrandTotal(grandTotal);

        invoice.setPaymentMethod(order.getPaymentStatus() != null && order.getPaymentStatus().equals("PAID") ? "ONLINE" : "COD");
        invoice.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING");

        // Save to get ID
        invoice = invoiceRepository.save(invoice);

        // Generate PDF
        String pdfPath = createPDF(invoice);
        invoice.setPdfPath(pdfPath);

        invoiceRepository.save(invoice);
    }

    /**
     * Generate unique invoice number
     * Format: INV-YYYYMMDD-XXXX
     */
    public String generateInvoiceNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = invoiceRepository.countByDate(LocalDate.now()) + 1;
        return String.format("INV-%s-%04d", dateStr, count);
    }

    /**
     * Create PDF invoice
     */
    public String createPDF(Invoice invoice) throws FileNotFoundException {
        // Create directory if not exists
        File dir = new File(invoiceStoragePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = invoice.getInvoiceNumber() + ".pdf";
        String filePath = invoiceStoragePath + File.separator + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Colors
        DeviceRgb purple = new DeviceRgb(124, 58, 237);

        // Header
        Paragraph header = new Paragraph("WATCHSHOP")
                .setFontSize(24)
                .setBold()
                .setFontColor(purple);
        document.add(header);

        document.add(new Paragraph("Luxury Timepieces")
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY));

        document.add(new Paragraph("\n"));

        // Invoice Info
        Table infoTable = new Table(2);
        infoTable.setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(new Cell().add(new Paragraph("Invoice Number:").setBold()).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(invoice.getInvoiceNumber())).setBorder(null));

        infoTable.addCell(new Cell().add(new Paragraph("Date:").setBold()).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(invoice.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")))).setBorder(null));

        infoTable.addCell(new Cell().add(new Paragraph("Order ID:").setBold()).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(String.valueOf(invoice.getOrder().getId()))).setBorder(null));

        document.add(infoTable);
        document.add(new Paragraph("\n"));

        // Customer Details
        document.add(new Paragraph("Bill To:").setBold().setFontSize(14));
        document.add(new Paragraph(invoice.getUser().getUsername()));
        document.add(new Paragraph(invoice.getUser().getEmail()));

        document.add(new Paragraph("\n"));

        // Items Table
        Table itemsTable = new Table(new float[]{3, 1, 2, 2});
        itemsTable.setWidth(UnitValue.createPercentValue(100));

        // Header
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Product").setBold()).setBackgroundColor(new DeviceRgb(240, 240, 240)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold()).setBackgroundColor(new DeviceRgb(240, 240, 240)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Price").setBold()).setBackgroundColor(new DeviceRgb(240, 240, 240)));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()).setBackgroundColor(new DeviceRgb(240, 240, 240)));

        // Items
        for (OrderItem item : invoice.getOrder().getOrderItems()) {
            itemsTable.addCell(item.getProduct().getName());
            itemsTable.addCell(String.valueOf(item.getQuantity()));
            itemsTable.addCell("₹" + String.format("%,.2f", item.getPrice()));
            itemsTable.addCell("₹" + String.format("%,.2f", item.getPrice() * item.getQuantity()));
        }

        document.add(itemsTable);
        document.add(new Paragraph("\n"));

        // Totals
        Table totalsTable = new Table(new float[]{3, 1});
        totalsTable.setWidth(UnitValue.createPercentValue(50));
        totalsTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        totalsTable.addCell(new Cell().add(new Paragraph("Subtotal:")).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(new Cell().add(new Paragraph("₹" + String.format("%,.2f", invoice.getSubtotal()))).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        totalsTable.addCell(new Cell().add(new Paragraph("GST (18%):")).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(new Cell().add(new Paragraph("₹" + String.format("%,.2f", invoice.getTax()))).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        totalsTable.addCell(new Cell().add(new Paragraph("Grand Total:").setBold().setFontSize(14)).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(new Cell().add(new Paragraph("₹" + String.format("%,.2f", invoice.getGrandTotal())).setBold().setFontSize(14)).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        document.add(totalsTable);
        document.add(new Paragraph("\n"));

        // Payment Info
        document.add(new Paragraph("Payment Method: " + invoice.getPaymentMethod()).setFontSize(10));
        document.add(new Paragraph("Payment Status: " + invoice.getPaymentStatus()).setFontSize(10).setFontColor(
                invoice.getPaymentStatus().equals("PAID") ? ColorConstants.GREEN : ColorConstants.ORANGE
        ));

        document.add(new Paragraph("\n\n"));

        // Footer
        document.add(new Paragraph("Thank you for your purchase!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setFontSize(10));

        document.close();

        return filePath;
    }

    /**
     * Get invoice by order ID
     */
    public Optional<Invoice> getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId);
    }

    /**
     * Get all invoices for a user
     */
    public List<Invoice> getUserInvoices(Long userId) {
        return invoiceRepository.findByUserIdOrderByGeneratedAtDesc(userId);
    }
}
