package com.pharmacy.model;

/**
 * POS cart item — transient, not persisted.
 * Demonstrates: Encapsulation, Composition
 */
public class CartItem {

    private Medicine medicine;
    private int      quantity;

    public CartItem(Medicine medicine, int quantity) {
        if (medicine == null) throw new IllegalArgumentException("Medicine cannot be null");
        if (quantity  <= 0)   throw new IllegalArgumentException("Quantity must be positive");
        this.medicine = medicine;
        this.quantity = quantity;
    }

    public double  getSubtotal()           { return medicine.getUnitPrice() * quantity; }
    public Medicine getMedicine()          { return medicine; }
    public int     getQuantity()           { return quantity; }
    public void    setQuantity(int v)      {
        if (v <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = v;
    }
    public String  getMedicineName()       { return medicine.getName(); }
    public double  getUnitPrice()          { return medicine.getUnitPrice(); }
    public int     getMedId()              { return medicine.getId(); }
    public boolean isNarcotic()            { return medicine.isNarcotic(); }

    @Override
    public String toString() {
        return medicine.getName() + " x" + quantity + " = Rs " + String.format("%.2f", getSubtotal());
    }
}
