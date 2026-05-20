package com.pharmacy.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private String invoiceID;
    private Sale sale;
    private LocalDateTime generatedAt;
    private String pharmacistID;

    public Invoice() {
        this.generatedAt = LocalDateTime.now();
    }

    public Invoice(String invoiceID, Sale sale, String pharmacistID) {
        this();
        this.invoiceID = invoiceID;
        this.sale = sale;
        this.pharmacistID = pharmacistID;
    }

    public String format() {
        double total = sale != null ? sale.getGrandTotal() : 0;
        return String.format("Invoice %s%nGenerated: %s%nPharmacist: %s%nTotal: Rs %.2f",
            invoiceID, generatedAt, pharmacistID, total);
    }

    public void print() {
        System.out.println(format());
    }

    public String getInvoiceID() { return invoiceID; }
    public void setInvoiceID(String v) { this.invoiceID = v; }
    public Sale getSale() { return sale; }
    public void setSale(Sale v) { this.sale = v; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime v) { this.generatedAt = v; }
    public String getPharmacistID() { return pharmacistID; }
    public void setPharmacistID(String v) { this.pharmacistID = v; }
}
