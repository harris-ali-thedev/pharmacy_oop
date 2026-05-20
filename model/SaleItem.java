package com.pharmacy.model;

/**
 * Persisted sale line item.
 * Demonstrates: Inheritance, Encapsulation
 */
public class SaleItem extends Entity {

    private static final long serialVersionUID = 1L;

    private int    saleId;
    private int    medId;
    private String medicineName;
    private Medicine medicine;
    private int    quantity;
    private double unitPrice;
    private double subtotal;

    public SaleItem() { super(); }

    public SaleItem(int medId, String medicineName, int quantity, double unitPrice) {
        super();
        this.medId        = medId;
        this.medicineName = medicineName;
        this.quantity     = quantity;
        this.unitPrice    = unitPrice;
        this.subtotal     = unitPrice * quantity;
    }

    public SaleItem(Medicine medicine, int quantity) {
        this(medicine != null ? medicine.getId() : 0,
             medicine != null ? medicine.getName() : null,
             quantity,
             medicine != null ? medicine.getUnitPrice() : 0);
        this.medicine = medicine;
    }

    @Override public String  getDisplayName() { return medicineName + " x" + quantity; }
    @Override public boolean isValid()        { return medId > 0 && quantity > 0 && unitPrice >= 0; }

    /** Factory: build a SaleItem from a CartItem */
    public static SaleItem fromCartItem(CartItem ci) {
        return new SaleItem(ci.getMedId(), ci.getMedicineName(), ci.getQuantity(), ci.getUnitPrice());
    }

    public double computeLineTotal() {
        subtotal = unitPrice * quantity;
        return subtotal;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public int    getSaleId()               { return saleId; }
    public void   setSaleId(int v)          { this.saleId = v; }
    public int    getMedId()                { return medId; }
    public void   setMedId(int v)           { this.medId = v; }
    public String getMedicineName()         { return medicineName; }
    public void   setMedicineName(String v) { this.medicineName = v; }
    public void   setMedicine(Medicine v)   {
        this.medicine = v;
        if (v != null) {
            this.medId = v.getId();
            this.medicineName = v.getName();
            this.unitPrice = v.getUnitPrice();
        }
    }
    public int    getQuantity()             { return quantity; }
    public void   setQuantity(int v)        { this.quantity = v; }
    public double getUnitPrice()            { return unitPrice; }
    public void   setUnitPrice(double v)    { this.unitPrice = v; }
    public double getSubtotal()             { return subtotal; }
    public void   setSubtotal(double v)     { this.subtotal = v; }
}
