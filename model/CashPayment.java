package com.pharmacy.model;

public class CashPayment implements Payable {
    private static final long serialVersionUID = 1L;

    private double amountTendered;
    private double change;

    public CashPayment() {}

    public CashPayment(double amountTendered) {
        this.amountTendered = amountTendered;
    }

    @Override
    public void processPayment(double amount) {
        if (amountTendered < amount) throw new IllegalArgumentException("Insufficient cash tendered");
        change = amountTendered - amount;
    }

    @Override
    public String generateReceipt() {
        return String.format("Cash payment | Tendered: Rs %.2f | Change: Rs %.2f", amountTendered, change);
    }

    @Override
    public String getPaymentType() {
        return "Cash";
    }

    public double computeChange() { return change; }
    public double getAmountTendered() { return amountTendered; }
    public void setAmountTendered(double v) { this.amountTendered = v; }
    public double getChange() { return change; }
    public void setChange(double v) { this.change = v; }
}
