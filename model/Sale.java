package com.pharmacy.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Sale extends Entity {

    private static final long serialVersionUID = 1L;

    public enum PaymentMethod { CASH, CARD, DIGITAL_WALLET }
    public enum Status        { COMPLETED, VOIDED }

    private int           userId;
    private int           patientId;
    private String        cashierName;
    private String        patientName;
    private LocalDateTime saleDate;
    private double        subtotal;
    private double        discountAmount;
    private double        taxPercent;
    private double        taxAmount;
    private double        grandTotal;
    private double        amountPaid;
    private double        changeReturned;
    private double        tax;
    private PaymentMethod paymentMethod;
    private Payable       payment;
    private Status        status;
    private List<SaleItem> items;

    public Sale() {
        super();
        this.saleDate      = LocalDateTime.now();
        this.status        = Status.COMPLETED;
        this.paymentMethod = PaymentMethod.CASH;
        this.items         = new ArrayList<>();
    }

    @Override public String getDisplayName() {
        return "Sale #" + getId() + " — Rs " + String.format("%.2f", grandTotal);
    }
    @Override public boolean isValid() {
        return userId > 0 && grandTotal >= 0;
    }

    public boolean isWalkIn() { return patientId <= 0; }
    public boolean isVoided() { return status == Status.VOIDED; }

    public void addItem(SaleItem item) {
        if (item != null) {
            items.add(item);
            computeTotals();
        }
    }

    public void computeTotals() {
        subtotal = items.stream().mapToDouble(SaleItem::computeLineTotal).sum();
        taxAmount = subtotal * taxPercent / 100.0;
        tax = taxAmount;
        grandTotal = subtotal - discountAmount + taxAmount;
    }

    public Invoice generateInvoice() {
        return new Invoice("INV-" + getId(), this, String.valueOf(userId));
    }

    public void processPayment() {
        if (payment != null) payment.processPayment(grandTotal);
    }

    public int           getUserId()                      { return userId; }
    public void          setUserId(int v)                 { this.userId = v; }
    public int           getPatientId()                   { return patientId; }
    public void          setPatientId(int v)              { this.patientId = v; }
    public String        getCashierName()                 { return cashierName; }
    public void          setCashierName(String v)         { this.cashierName = v; }
    public String        getPatientName()                 { return patientName; }
    public void          setPatientName(String v)         { this.patientName = v; }
    public LocalDateTime getSaleDate()                    { return saleDate; }
    public void          setSaleDate(LocalDateTime v)     { this.saleDate = v; }
    public double        getSubtotal()                    { return subtotal; }
    public void          setSubtotal(double v)            { this.subtotal = v; }
    public double        getDiscountAmount()              { return discountAmount; }
    public void          setDiscountAmount(double v)      { this.discountAmount = v; }
    public double        getTaxPercent()                  { return taxPercent; }
    public void          setTaxPercent(double v)          { this.taxPercent = v; }
    public double        getTaxAmount()                   { return taxAmount; }
    public void          setTaxAmount(double v)           { this.taxAmount = v; this.tax = v; }
    public double        getTax()                         { return taxAmount; }
    public void          setTax(double v)                 { setTaxAmount(v); }
    public double        getGrandTotal()                  { return grandTotal; }
    public void          setGrandTotal(double v)          { this.grandTotal = v; }
    public double        getAmountPaid()                  { return amountPaid; }
    public void          setAmountPaid(double v)          { this.amountPaid = v; }
    public double        getChangeReturned()              { return changeReturned; }
    public void          setChangeReturned(double v)      { this.changeReturned = v; }
    public PaymentMethod getPaymentMethod()               { return paymentMethod; }
    public void          setPaymentMethod(PaymentMethod v){ this.paymentMethod = v; }
    public Payable       getPayment()                     { return payment; }
    public void          setPayment(Payable v)            { this.payment = v; }
    public LocalDateTime getSaleDateTime()                { return saleDate; }
    public void          setSaleDateTime(LocalDateTime v) { this.saleDate = v; }
    public Status        getStatus()                      { return status; }
    public void          setStatus(Status v)              { this.status = v; }
    public List<SaleItem> getItems()                      { return items; }
    public void          setItems(List<SaleItem> v)       { this.items = v != null ? v : new ArrayList<>(); }

    @Override
    public String toString() {
        return String.format("Sale #%d | %s | Rs %.2f | %s",
            getId(), saleDate.toLocalDate(), grandTotal, status);
    }
}
