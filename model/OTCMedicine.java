package com.pharmacy.model;

public class OTCMedicine extends Medicine {
    private static final long serialVersionUID = 1L;

    private boolean isDiscountEligible = true;
    private double discountRate;

    @Override
    public String getDetails() {
        return super.getDetails() + " | OTC";
    }

    public double getDiscountedPrice() {
        return getUnitPrice() - (getUnitPrice() * discountRate / 100.0);
    }

    public boolean isDiscountEligible() { return isDiscountEligible; }
    public void setDiscountEligible(boolean v) { this.isDiscountEligible = v; }
    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double v) { this.discountRate = Math.max(0, v); }
}
